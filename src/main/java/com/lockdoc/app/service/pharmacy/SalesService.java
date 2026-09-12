package com.lockdoc.app.service.pharmacy;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.SalesInvoiceItemRequest;
import com.lockdoc.app.dto.pharmacy.SalesInvoiceRequest;
import com.lockdoc.app.dto.pharmacy.SalesInvoiceResponse;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.Store;
import com.lockdoc.app.entity.pharmacy.Medicine;
import com.lockdoc.app.entity.pharmacy.MedicineBatch;
import com.lockdoc.app.entity.pharmacy.Patient;
import com.lockdoc.app.entity.pharmacy.SalesInvoice;
import com.lockdoc.app.entity.pharmacy.SalesInvoiceItem;
import com.lockdoc.app.entity.pharmacy.StatutoryRegisterEntry;
import com.lockdoc.app.entity.pharmacy.StockLedgerEntry;
import com.lockdoc.app.exception.InsufficientStockException;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.exception.SafetyCheckException;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.pharmacy.MedicineBatchRepository;
import com.lockdoc.app.repository.pharmacy.MedicineRepository;
import com.lockdoc.app.repository.pharmacy.PatientRepository;
import com.lockdoc.app.repository.pharmacy.SalesInvoiceRepository;
import com.lockdoc.app.repository.pharmacy.SalesReturnRepository;
import com.lockdoc.app.repository.pharmacy.StatutoryRegisterEntryRepository;
import com.lockdoc.app.repository.pharmacy.StockLedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesService {

    private static final String DOC_TYPE = "SALE";
    private static final String PREFIX = "INV";

    private static final String STATUS_POSTED = "POSTED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private static final String TXN_TYPE_SALE = "SALE";
    private static final String REFERENCE_TYPE_SALE = "SALE";

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final PatientRepository patientRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository medicineBatchRepository;
    private final SalesReturnRepository salesReturnRepository;
    private final StockLedgerEntryRepository stockLedgerEntryRepository;
    private final StatutoryRegisterEntryRepository statutoryRegisterEntryRepository;
    private final FacilityRepository facilityRepository;
    private final StoreResolutionService storeResolutionService;
    private final DocumentNumberService documentNumberService;

    public PageResponse<SalesInvoiceResponse> list(int page, int size, String search) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Pageable pageable = PageRequest.of(page, size);
        Page<SalesInvoice> result = StringUtils.hasText(search)
                ? salesInvoiceRepository.search(facilityId, search, pageable)
                : salesInvoiceRepository.findByFacilityId(facilityId, pageable);
        return PageResponse.of(result, SalesInvoiceResponse::toResponse);
    }

    public SalesInvoiceResponse get(Long id) {
        return SalesInvoiceResponse.toResponse(findEntity(id));
    }

    public SalesInvoiceResponse create(SalesInvoiceRequest request, Long userId) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Facility facility = facilityRepository.getReferenceById(facilityId);
        Store store = storeResolutionService.resolveDefaultStore(facilityId);

        Patient patient = null;
        if (request.getPatientId() != null) {
            patient = patientRepository.findByIdAndFacilityId(request.getPatientId(), facilityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));
        }

        // Pass 1: resolve medicine+batch, validate stock, and run every
        // dispensing safety check (Master Spec §11.5) BEFORE mutating any
        // stock - a SafetyCheckException or a hard validation failure here
        // must leave every batch untouched, not partially decremented.
        record ResolvedLine(SalesInvoiceItemRequest request, Medicine medicine, MedicineBatch batch) {}
        List<ResolvedLine> resolved = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> genericNamesSeen = new HashSet<>();

        for (SalesInvoiceItemRequest itemRequest : request.getItems()) {
            Medicine medicine = medicineRepository.findByIdAndFacilityId(itemRequest.getMedicineId(), facilityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + itemRequest.getMedicineId()));

            MedicineBatch batch = medicineBatchRepository.findById(itemRequest.getMedicineBatchId())
                    .filter(b -> b.getFacility().getId().equals(facilityId))
                    .orElseThrow(() -> new ResourceNotFoundException("Medicine batch not found with id: " + itemRequest.getMedicineBatchId()));

            // Pre-existing gap, fixed here while already touching this method:
            // nothing previously checked the batch actually belongs to the
            // requested medicine, so a mismatched (but validly-existing) pair
            // would silently decrement the wrong medicine's stock.
            if (!batch.getMedicine().getId().equals(medicine.getId())) {
                throw new InvalidDocumentStateException(
                        "Batch " + batch.getBatchNo() + " belongs to a different medicine than the one requested");
            }

            if (batch.getQuantityOnHand() < itemRequest.getQty()) {
                throw new InsufficientStockException(
                        "Insufficient stock for medicine " + medicine.getName() + " batch " + batch.getBatchNo()
                                + " - available: " + batch.getQuantityOnHand() + ", requested: " + itemRequest.getQty());
            }

            // Mandatory prescriber capture (§11.5) - a hard failure, not a
            // soft-stop warning; §11.5 calls this "mandatory", full stop.
            if (medicine.requiresPrescriberCapture()
                    && (!StringUtils.hasText(itemRequest.getPrescriberName()) || !StringUtils.hasText(itemRequest.getPrescriberRegistrationNumber()))) {
                throw new IllegalArgumentException(
                        "Prescriber name and registration number are required to dispense " + medicine.getName()
                                + " (Schedule " + medicine.getDrugSchedule() + ")");
            }

            boolean overridden = StringUtils.hasText(itemRequest.getOverrideReason());
            if (!overridden) {
                if (patient != null && StringUtils.hasText(patient.getAllergies()) && matchesAllergy(patient.getAllergies(), medicine)) {
                    warnings.add("Patient has a recorded allergy that may match " + medicine.getName()
                            + " (" + (StringUtils.hasText(medicine.getGenericName()) ? medicine.getGenericName() : medicine.getName())
                            + ") - recorded allergies: " + patient.getAllergies());
                }
                if (Boolean.TRUE.equals(medicine.getHighAlert())) {
                    warnings.add(medicine.getName() + " is a high-alert medication - confirm before dispensing");
                }
                if (StringUtils.hasText(medicine.getGenericName()) && !genericNamesSeen.add(medicine.getGenericName().toLowerCase(Locale.ROOT))) {
                    warnings.add("Duplicate therapy: " + medicine.getGenericName() + " appears in more than one line on this sale");
                }
            }

            resolved.add(new ResolvedLine(itemRequest, medicine, batch));
        }

        if (!warnings.isEmpty()) {
            throw new SafetyCheckException(warnings);
        }

        // Pass 2: every line cleared - now it's safe to actually deduct stock.
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;

        List<SalesInvoiceItem> items = new ArrayList<>();
        for (ResolvedLine line : resolved) {
            SalesInvoiceItemRequest itemRequest = line.request();
            Medicine medicine = line.medicine();
            MedicineBatch batch = line.batch();

            BigDecimal taxPercent = itemRequest.getTaxPercent() != null ? itemRequest.getTaxPercent() : BigDecimal.ZERO;
            BigDecimal lineDiscount = itemRequest.getDiscountAmount() != null ? itemRequest.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal lineBase = itemRequest.getRate().multiply(BigDecimal.valueOf(itemRequest.getQty()));
            BigDecimal lineTax = lineBase.multiply(taxPercent).divide(BigDecimal.valueOf(100));
            BigDecimal lineAmount = lineBase.add(lineTax).subtract(lineDiscount);

            subtotal = subtotal.add(lineBase);
            taxAmount = taxAmount.add(lineTax);
            discountAmount = discountAmount.add(lineDiscount);

            batch.setQuantityOnHand(batch.getQuantityOnHand() - itemRequest.getQty());
            medicineBatchRepository.save(batch);

            items.add(SalesInvoiceItem.builder()
                    .medicine(medicine)
                    .medicineBatch(batch)
                    .qty(itemRequest.getQty())
                    .rate(itemRequest.getRate())
                    .taxPercent(taxPercent)
                    .discountAmount(lineDiscount)
                    .amount(lineAmount)
                    .prescriptionLineId(itemRequest.getPrescriptionLineId())
                    .overrideReason(itemRequest.getOverrideReason())
                    .prescriberName(itemRequest.getPrescriberName())
                    .prescriberRegistrationNumber(itemRequest.getPrescriberRegistrationNumber())
                    .build());
        }

        BigDecimal totalAmount = subtotal.add(taxAmount).subtract(discountAmount);
        // Retail pharmacy convention: cash collected is rounded to the nearest
        // rupee; roundOffAmount records that adjustment explicitly instead of
        // letting it vanish - previously balanceDue silently absorbed it because
        // amountPaid defaulted to the unrounded totalAmount.
        BigDecimal payableAmount = totalAmount.setScale(0, RoundingMode.HALF_UP);
        BigDecimal roundOffAmount = payableAmount.subtract(totalAmount);
        BigDecimal amountPaid = request.getAmountPaid() != null ? request.getAmountPaid() : payableAmount;
        BigDecimal balanceDue = payableAmount.subtract(amountPaid);

        SalesInvoice invoice = SalesInvoice.builder()
                .facility(facility)
                .store(store)
                .invoiceNumber(documentNumberService.next(facilityId, DOC_TYPE, PREFIX))
                .patient(patient)
                .walkInCustomerName(request.getWalkInCustomerName())
                .walkInCustomerPhone(request.getWalkInCustomerPhone())
                .saleDate(request.getSaleDate())
                .paymentMode(request.getPaymentMode())
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .amountPaid(amountPaid)
                .balanceDue(balanceDue)
                .roundOffAmount(roundOffAmount)
                .status(STATUS_POSTED)
                .createdBy(userId)
                .items(new ArrayList<>())
                .build();
        for (SalesInvoiceItem item : items) {
            item.setSalesInvoice(invoice);
        }
        invoice.setItems(items);

        invoice = salesInvoiceRepository.save(invoice);

        for (SalesInvoiceItem item : invoice.getItems()) {
            stockLedgerEntryRepository.save(StockLedgerEntry.builder()
                    .facility(facility)
                    .store(store)
                    .medicine(item.getMedicine())
                    .medicineBatch(item.getMedicineBatch())
                    .txnType(TXN_TYPE_SALE)
                    .qtyIn(0)
                    .qtyOut(item.getQty())
                    .balanceQty(item.getMedicineBatch().getQuantityOnHand())
                    .referenceType(REFERENCE_TYPE_SALE)
                    .referenceId(invoice.getId())
                    .referenceNumber(invoice.getInvoiceNumber())
                    .txnDate(LocalDateTime.now())
                    .build());

            // Statutory register (§6/§15.2, build order step 7) - auto-created for
            // every scheduled-drug line, reusing exactly the prescriber capture
            // step 6 already made mandatory at the point of sale for this reason.
            if (item.getMedicine().requiresPrescriberCapture()) {
                statutoryRegisterEntryRepository.save(StatutoryRegisterEntry.builder()
                        .facility(facility)
                        .registerType(StatutoryRegisterEntry.registerTypeFor(item.getMedicine().getDrugSchedule()))
                        .salesInvoiceItemId(item.getId())
                        .medicineName(item.getMedicine().getName())
                        .patientName(patient != null ? patient.getFullName() : invoice.getWalkInCustomerName())
                        .qty(item.getQty())
                        .prescriberName(item.getPrescriberName())
                        .prescriberRegistrationNumber(item.getPrescriberRegistrationNumber())
                        .saleDate(invoice.getSaleDate())
                        .build());
            }
        }

        return SalesInvoiceResponse.toResponse(invoice);
    }

    public SalesInvoiceResponse cancel(Long id) {
        SalesInvoice invoice = findEntity(id);
        if (!STATUS_POSTED.equals(invoice.getStatus())) {
            throw new InvalidDocumentStateException("Sales invoice cannot be cancelled from status: " + invoice.getStatus());
        }
        if (salesReturnRepository.existsBySalesInvoiceId(invoice.getId())) {
            throw new InvalidDocumentStateException("Sales invoice cannot be cancelled - sales returns already exist against it");
        }

        for (SalesInvoiceItem item : invoice.getItems()) {
            MedicineBatch batch = item.getMedicineBatch();
            batch.setQuantityOnHand(batch.getQuantityOnHand() + item.getQty());
            medicineBatchRepository.save(batch);

            stockLedgerEntryRepository.save(StockLedgerEntry.builder()
                    .facility(invoice.getFacility())
                    .store(invoice.getStore())
                    .medicine(item.getMedicine())
                    .medicineBatch(batch)
                    .txnType(TXN_TYPE_SALE)
                    .qtyIn(item.getQty())
                    .qtyOut(0)
                    .balanceQty(batch.getQuantityOnHand())
                    .referenceType(REFERENCE_TYPE_SALE)
                    .referenceId(invoice.getId())
                    .referenceNumber(invoice.getInvoiceNumber())
                    .txnDate(LocalDateTime.now())
                    .build());
        }

        invoice.setStatus(STATUS_CANCELLED);
        return SalesInvoiceResponse.toResponse(salesInvoiceRepository.save(invoice));
    }

    /**
     * Simple, deliberately conservative substring match between a
     * patient's free-text allergy list and a medicine's name/generic name
     * (Master Spec §11.5) - a real drug-allergen ontology is out of scope
     * for this build (no such reference dataset exists here yet); this
     * catches the common case (patient's recorded allergy text names the
     * same drug/molecule) without claiming clinical completeness. Always
     * a soft-stop the dispensing user can override with a reason, never a
     * hard block, precisely because it's a heuristic, not a real check.
     */
    private boolean matchesAllergy(String allergies, Medicine medicine) {
        String haystack = allergies.toLowerCase(Locale.ROOT);
        // Arrays.asList, not List.of - a null genericName (common, optional field) would
        // otherwise NPE inside List.of's null-rejecting constructor before hasText below ever runs.
        for (String candidate : java.util.Arrays.asList(medicine.getName(), medicine.getGenericName())) {
            if (StringUtils.hasText(candidate)) {
                for (String token : candidate.toLowerCase(Locale.ROOT).split("[,\\s]+")) {
                    if (token.length() >= 4 && haystack.contains(token)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private SalesInvoice findEntity(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return salesInvoiceRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales invoice not found with id: " + id));
    }
}
