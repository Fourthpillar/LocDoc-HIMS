package com.lockdoc.app.service.op;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.op.BillResponse;
import com.lockdoc.app.dto.op.DiscountRequest;
import com.lockdoc.app.dto.op.DiscountResponse;
import com.lockdoc.app.dto.op.PaymentRequest;
import com.lockdoc.app.dto.op.PaymentResponse;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.op.ApprovalPolicy;
import com.lockdoc.app.entity.op.Bill;
import com.lockdoc.app.entity.op.Discount;
import com.lockdoc.app.entity.op.Payment;
import com.lockdoc.app.entity.pharmacy.Patient;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.op.ApprovalPolicyRepository;
import com.lockdoc.app.repository.op.BillRepository;
import com.lockdoc.app.repository.op.DiscountRepository;
import com.lockdoc.app.repository.op.PaymentRepository;
import com.lockdoc.app.service.pharmacy.DocumentNumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * The single billing engine for every OP encounter type (Master Spec
 * §6/§7.5) - Bill is the one source of truth for gross/discount/net/
 * paid/due, never duplicated per encounter (see PatientRegistration's
 * own javadoc for the reasoning). Registration and consultation billing
 * (this build order step) both call {@link #createBill} rather than each
 * rolling their own totals.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BillingService {

    private static final String DOC_TYPE = "OP_BILL";
    private static final String PREFIX = "BILL";

    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final DiscountRepository discountRepository;
    private final ApprovalPolicyRepository approvalPolicyRepository;
    private final DocumentNumberService documentNumberService;

    /** Called by other OP services (registration, consultation) - not exposed as its own public endpoint. */
    public Bill createBill(Facility facility, Patient patient, String encounterType, Long encounterId, BigDecimal gross, Long createdByUserId) {
        // A ₹0 bill (a free review, §7.4) has nothing to collect - mark it
        // PAID immediately rather than leaving it UNPAID forever with a ₹0
        // due, which would otherwise clutter Due Collection with rows no
        // one can ever "pay off".
        boolean nothingOwed = gross.compareTo(BigDecimal.ZERO) <= 0;
        Bill bill = Bill.builder()
                .facility(facility)
                .patient(patient)
                .billNo(documentNumberService.next(facility.getId(), DOC_TYPE, PREFIX))
                .encounterType(encounterType)
                .encounterId(encounterId)
                .gross(gross)
                .discount(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .net(gross)
                .paid(BigDecimal.ZERO)
                .due(gross)
                .status(nothingOwed ? Bill.STATUS_PAID : Bill.STATUS_UNPAID)
                .createdByUserId(createdByUserId)
                .build();
        return billRepository.save(bill);
    }

    public BillResponse get(Long id) {
        return BillResponse.toResponse(findEntity(id));
    }

    public BillResponse getByEncounter(String encounterType, Long encounterId) {
        return billRepository.findByEncounterTypeAndEncounterId(encounterType, encounterId)
                .map(BillResponse::toResponse)
                .orElse(null);
    }

    /** Blank/absent search returns every outstanding bill at the facility - the Due Collection screen's default view (§17.7 #8). */
    public List<BillResponse> searchDue(String search) {
        Long facilityId = SecurityUtils.requireFacilityId();
        if (!org.springframework.util.StringUtils.hasText(search)) {
            return billRepository
                    .findByFacilityIdAndStatusInOrderByCreatedDateDesc(facilityId, List.of(Bill.STATUS_UNPAID, Bill.STATUS_PARTIALLY_PAID), org.springframework.data.domain.Pageable.unpaged())
                    .stream().map(BillResponse::toResponse).toList();
        }
        return billRepository.searchDue(facilityId, search).stream().map(BillResponse::toResponse).toList();
    }

    /** Every bill for one patient at this facility, newest first - settled or not, unlike searchDue which is the outstanding worklist. */
    public List<BillResponse> listForPatient(Long patientId) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return billRepository.findByPatientIdAndFacilityIdOrderByCreatedDateDesc(patientId, facilityId)
                .stream().map(BillResponse::toResponse).toList();
    }

    public PaymentResponse recordPayment(Long billId, PaymentRequest request) {
        Long userId = SecurityUtils.currentUserId();
        Bill bill = findEntity(billId);

        if (Bill.STATUS_CANCELLED.equals(bill.getStatus())) {
            throw new InvalidDocumentStateException("Cannot record a payment against a cancelled bill");
        }
        if (request.getAmount().compareTo(bill.getDue()) > 0) {
            throw new InvalidDocumentStateException(
                    "Payment amount (" + request.getAmount() + ") exceeds the due amount (" + bill.getDue() + ")");
        }

        Payment payment = Payment.builder()
                .bill(bill)
                .facility(bill.getFacility())
                .partyType(request.getPartyType() != null ? request.getPartyType() : Payment.PARTY_PATIENT)
                .paymentType(request.getPaymentType())
                .amount(request.getAmount())
                .receivedByUserId(userId)
                .build();
        paymentRepository.save(payment);

        bill.setPaid(bill.getPaid().add(request.getAmount()));
        bill.setDue(bill.getNet().subtract(bill.getPaid()));
        bill.setStatus(bill.getDue().compareTo(BigDecimal.ZERO) <= 0 ? Bill.STATUS_PAID : Bill.STATUS_PARTIALLY_PAID);
        billRepository.save(bill);

        return PaymentResponse.toResponse(payment);
    }

    public List<PaymentResponse> listPayments(Long billId) {
        findEntity(billId); // facility-scope check
        return paymentRepository.findByBillIdOrderByPaidAtAsc(billId).stream().map(PaymentResponse::toResponse).toList();
    }

    /**
     * Below the facility's DISCOUNT threshold (ApprovalPolicy) it posts
     * straight to APPROVED and reduces the bill now; at or above it, it
     * starts PENDING_APPROVAL and the bill is untouched until a Hospital/
     * Clinic Admin approves it (Master Spec §7.5).
     */
    public DiscountResponse requestDiscount(Long billId, DiscountRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Long userId = SecurityUtils.currentUserId();
        Bill bill = findEntity(billId);

        BigDecimal amount = resolveDiscountAmount(bill, request);
        if (amount.compareTo(bill.getNet().subtract(bill.getDiscount())) > 0) {
            throw new InvalidDocumentStateException("Discount amount cannot exceed the bill's current payable amount");
        }

        BigDecimal threshold = approvalPolicyRepository.findByFacilityIdAndApprovalType(facilityId, ApprovalPolicy.TYPE_DISCOUNT)
                .map(ApprovalPolicy::getThresholdValue)
                .orElse(BigDecimal.ZERO); // no policy set - facility hasn't configured self-service yet, so nothing auto-approves

        boolean autoApprove = amount.compareTo(threshold) < 0;

        Discount discount = Discount.builder()
                .bill(bill)
                .facility(bill.getFacility())
                .discountKind(request.getDiscountKind())
                .value(request.getValue())
                .amount(amount)
                .reason(request.getReason())
                .status(autoApprove ? Discount.STATUS_APPROVED : Discount.STATUS_PENDING_APPROVAL)
                .requestedByUserId(userId)
                .build();

        if (autoApprove) {
            discount.setApprovedByUserId(userId);
            discount.setApprovedAt(java.time.LocalDateTime.now());
            applyDiscountToBill(bill, amount);
        }
        discount = discountRepository.save(discount);
        return DiscountResponse.toResponse(discount);
    }

    public DiscountResponse approveDiscount(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Long userId = SecurityUtils.currentUserId();
        Discount discount = discountRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + id));
        if (!Discount.STATUS_PENDING_APPROVAL.equals(discount.getStatus())) {
            throw new InvalidDocumentStateException("Discount can only be approved from PENDING_APPROVAL, current status: " + discount.getStatus());
        }
        discount.setStatus(Discount.STATUS_APPROVED);
        discount.setApprovedByUserId(userId);
        discount.setApprovedAt(java.time.LocalDateTime.now());
        applyDiscountToBill(discount.getBill(), discount.getAmount());
        return DiscountResponse.toResponse(discountRepository.save(discount));
    }

    public DiscountResponse rejectDiscount(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Long userId = SecurityUtils.currentUserId();
        Discount discount = discountRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + id));
        if (!Discount.STATUS_PENDING_APPROVAL.equals(discount.getStatus())) {
            throw new InvalidDocumentStateException("Discount can only be rejected from PENDING_APPROVAL, current status: " + discount.getStatus());
        }
        discount.setStatus(Discount.STATUS_REJECTED);
        discount.setApprovedByUserId(userId);
        discount.setApprovedAt(java.time.LocalDateTime.now());
        return DiscountResponse.toResponse(discountRepository.save(discount));
    }

    public List<DiscountResponse> pendingDiscounts() {
        Long facilityId = SecurityUtils.requireFacilityId();
        return discountRepository.findByFacilityIdAndStatusOrderByCreatedDateAsc(facilityId, Discount.STATUS_PENDING_APPROVAL)
                .stream().map(DiscountResponse::toResponse).toList();
    }

    private BigDecimal resolveDiscountAmount(Bill bill, DiscountRequest request) {
        if (Discount.KIND_PERCENT.equals(request.getDiscountKind())) {
            if (request.getValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new InvalidDocumentStateException("Percent discount cannot exceed 100");
            }
            return bill.getGross().multiply(request.getValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return request.getValue();
    }

    private void applyDiscountToBill(Bill bill, BigDecimal amount) {
        bill.setDiscount(bill.getDiscount().add(amount));
        bill.setNet(bill.getGross().subtract(bill.getDiscount()).add(bill.getTax()));
        bill.setDue(bill.getNet().subtract(bill.getPaid()));
        bill.setStatus(bill.getDue().compareTo(BigDecimal.ZERO) <= 0 ? Bill.STATUS_PAID : bill.getPaid().compareTo(BigDecimal.ZERO) > 0 ? Bill.STATUS_PARTIALLY_PAID : Bill.STATUS_UNPAID);
        billRepository.save(bill);
    }

    /**
     * Cancelling a bill that already collected money doesn't refund it by itself - that
     * needs reception's own deliberate {@link #refund} action recording how the money
     * physically went back (§5 principle 4: no silent money movement). This just flags it.
     */
    void cancelBill(Bill bill) {
        bill.setStatus(Bill.STATUS_CANCELLED);
        bill.setRefundStatus(bill.getPaid().compareTo(BigDecimal.ZERO) > 0 ? Bill.REFUND_PENDING : Bill.REFUND_NONE);
        billRepository.save(bill);
    }

    /** Cancelled bills with money still owed back (§5 principle 4, §7.5) - the Refunds screen reads this. */
    public List<BillResponse> pendingRefunds() {
        Long facilityId = SecurityUtils.requireFacilityId();
        return billRepository.findByFacilityIdAndRefundStatusOrderByCreatedDateDesc(facilityId, Bill.REFUND_PENDING)
                .stream().map(BillResponse::toResponse).toList();
    }

    /**
     * Records the refund as a negative {@link Payment} - the same append-only ledger a
     * normal payment uses, so day-collection totals net it out correctly rather than a
     * cancelled bill's money silently staying "collected" forever. Supports a partial
     * refund (e.g. a processing fee retained): stays PENDING until nothing paid remains.
     */
    public PaymentResponse refund(Long billId, PaymentRequest request) {
        Long userId = SecurityUtils.currentUserId();
        Bill bill = findEntity(billId);
        if (!Bill.REFUND_PENDING.equals(bill.getRefundStatus())) {
            throw new InvalidDocumentStateException("This bill has no refund pending");
        }
        if (request.getAmount().compareTo(bill.getPaid()) > 0) {
            throw new InvalidDocumentStateException("Refund amount (" + request.getAmount() + ") exceeds what was paid (" + bill.getPaid() + ")");
        }

        Payment payment = Payment.builder()
                .bill(bill)
                .facility(bill.getFacility())
                .partyType(request.getPartyType() != null ? request.getPartyType() : Payment.PARTY_PATIENT)
                .paymentType(request.getPaymentType())
                .amount(request.getAmount().negate())
                .receivedByUserId(userId)
                .build();
        paymentRepository.save(payment);

        bill.setPaid(bill.getPaid().subtract(request.getAmount()));
        bill.setRefundStatus(bill.getPaid().compareTo(BigDecimal.ZERO) <= 0 ? Bill.REFUND_REFUNDED : Bill.REFUND_PENDING);
        billRepository.save(bill);

        return PaymentResponse.toResponse(payment);
    }

    Bill findEntity(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return billRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
    }
}
