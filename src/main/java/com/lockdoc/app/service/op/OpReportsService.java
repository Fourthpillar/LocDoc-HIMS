package com.lockdoc.app.service.op;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.op.*;
import com.lockdoc.app.entity.doctor.FreeReviewLink;
import com.lockdoc.app.entity.doctor.FreeReviewPolicy;
import com.lockdoc.app.entity.op.Bill;
import com.lockdoc.app.entity.op.Cancellation;
import com.lockdoc.app.entity.op.CommissionBasis;
import com.lockdoc.app.entity.op.Discount;
import com.lockdoc.app.entity.op.OpVisit;
import com.lockdoc.app.entity.op.PatientRegistration;
import com.lockdoc.app.entity.op.Payment;
import com.lockdoc.app.repository.doctor.FreeReviewLinkRepository;
import com.lockdoc.app.repository.doctor.FreeReviewPolicyRepository;
import com.lockdoc.app.repository.op.BillRepository;
import com.lockdoc.app.repository.op.CancellationRepository;
import com.lockdoc.app.repository.op.CommissionBasisRepository;
import com.lockdoc.app.repository.op.DiscountRepository;
import com.lockdoc.app.repository.op.OpVisitRepository;
import com.lockdoc.app.repository.op.PatientRegistrationRepository;
import com.lockdoc.app.repository.op.PaymentRepository;
import com.lockdoc.app.repository.pharmacy.SalesInvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OP Reports (Master Spec §17.7 #12a) - "OP had the largest report set of
 * any module in the source material (33 incumbent reports)... every
 * report on it follows the FilterBar rule: preset + custom date range,
 * PDF export". All eight named reports are built as of V47 (free reviews
 * in V45, commission attribution in V46, area master in V47).
 *
 * <p><b>Commission</b> (V46) matches {@link OpVisit#getReferralDoctor()}/
 * {@link OpVisit#getPro()} against {@link CommissionBasis} by
 * {@code partyName} (case-insensitive) - {@code CommissionBasis} itself
 * still keys on a free-text name, not the {@code ReferralDoctor}/{@code Pro}
 * id (its own javadoc: predates those master tables, V35 vs V37), so a
 * referrer with no matching-named, active basis configured reports a
 * null commission rather than a fabricated one. Per §8.1, this only ever
 * reports the computed share - it never transfers money.
 *
 * <p><b>Area-wise consultations</b> (V47) groups by {@code Patient.area} -
 * one flat Area master (country/state/city/area_name on one row), not
 * four normalized Country/State/City/Area tables; see V47's migration
 * comment. A patient with no area set groups under "Unspecified" rather
 * than being silently excluded.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OpReportsService {

    private final PatientRegistrationRepository registrationRepository;
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final DiscountRepository discountRepository;
    private final CancellationRepository cancellationRepository;
    private final OpVisitRepository opVisitRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final FreeReviewLinkRepository freeReviewLinkRepository;
    private final FreeReviewPolicyRepository freeReviewPolicyRepository;
    private final CommissionBasisRepository commissionBasisRepository;

    public RegistrationsReportResponse registrations(LocalDate from, LocalDate to) {
        Long facilityId = SecurityUtils.requireFacilityId();
        List<PatientRegistration> registrations = registrationRepository
                .findByFacilityIdAndRegisteredAtBetweenOrderByRegisteredAtAsc(facilityId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        List<RegistrationsReportRow> rows = new ArrayList<>();
        BigDecimal totalNet = BigDecimal.ZERO;
        BigDecimal totalCollected = BigDecimal.ZERO;
        int reRegCount = 0;

        for (PatientRegistration r : registrations) {
            Bill bill = billRepository.findByEncounterTypeAndEncounterId(Bill.ENCOUNTER_REGISTRATION, r.getId()).orElse(null);
            BigDecimal gross = bill != null ? bill.getGross() : BigDecimal.ZERO;
            BigDecimal discount = bill != null ? bill.getDiscount() : BigDecimal.ZERO;
            BigDecimal net = bill != null ? bill.getNet() : BigDecimal.ZERO;
            BigDecimal paid = bill != null ? bill.getPaid() : BigDecimal.ZERO;
            BigDecimal due = bill != null ? bill.getDue() : BigDecimal.ZERO;

            rows.add(RegistrationsReportRow.builder()
                    .patientName(r.getPatient().getFullName())
                    .patientMrn(r.getPatient().getMrn())
                    .registrationNo(r.getRegistrationNo())
                    .reRegistration(Boolean.TRUE.equals(r.getIsReRegistration()))
                    .registeredAt(r.getRegisteredAt())
                    .gross(gross)
                    .discount(discount)
                    .net(net)
                    .paid(paid)
                    .due(due)
                    .build());

            totalNet = totalNet.add(net);
            totalCollected = totalCollected.add(paid);
            if (Boolean.TRUE.equals(r.getIsReRegistration())) reRegCount++;
        }

        return RegistrationsReportResponse.builder()
                .rows(rows)
                .totalCount(rows.size())
                .reRegistrationCount(reRegCount)
                .totalNet(totalNet)
                .totalCollected(totalCollected)
                .build();
    }

    public DayCollectionReportResponse dayCollection(LocalDate from, LocalDate to) {
        Long facilityId = SecurityUtils.requireFacilityId();
        List<Payment> payments = paymentRepository.findByFacilityIdAndPaidAtBetweenOrderByPaidAtAsc(facilityId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        // Every date in range gets a row, even a zero one - a facility comparing days shouldn't see gaps.
        Map<LocalDate, BigDecimal[]> totals = new LinkedHashMap<>(); // [cash, cardUpi, cheque]
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            totals.put(d, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
        }

        for (Payment p : payments) {
            LocalDate date = p.getPaidAt().toLocalDate();
            BigDecimal[] bucket = totals.get(date);
            if (bucket == null) continue; // shouldn't happen given the query range, but guards a boundary edge
            int idx = Payment.TYPE_CASH.equals(p.getPaymentType()) ? 0 : Payment.TYPE_CARD_UPI.equals(p.getPaymentType()) ? 1 : 2;
            bucket[idx] = bucket[idx].add(p.getAmount());
        }

        List<DayCollectionRow> rows = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;
        for (Map.Entry<LocalDate, BigDecimal[]> e : totals.entrySet()) {
            BigDecimal[] b = e.getValue();
            BigDecimal dayTotal = b[0].add(b[1]).add(b[2]);
            rows.add(DayCollectionRow.builder().date(e.getKey()).cash(b[0]).cardUpi(b[1]).cheque(b[2]).total(dayTotal).build());
            grandTotal = grandTotal.add(dayTotal);
        }

        return DayCollectionReportResponse.builder().rows(rows).grandTotal(grandTotal).build();
    }

    public DiscountsReportResponse discounts(LocalDate from, LocalDate to) {
        Long facilityId = SecurityUtils.requireFacilityId();
        List<Discount> discounts = discountRepository.findByFacilityIdAndCreatedDateBetweenOrderByCreatedDateAsc(facilityId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        List<DiscountsReportRow> rows = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int pendingCount = 0;
        for (Discount d : discounts) {
            Bill bill = d.getBill();
            rows.add(DiscountsReportRow.builder()
                    .billNo(bill.getBillNo())
                    .patientName(bill.getPatient() != null ? bill.getPatient().getFullName() : "Walk-in")
                    .patientMrn(bill.getPatient() != null ? bill.getPatient().getMrn() : "—")
                    .discountKind(d.getDiscountKind())
                    .value(d.getValue())
                    .amount(d.getAmount())
                    .reason(d.getReason())
                    .status(d.getStatus())
                    .createdDate(d.getCreatedDate())
                    .build());
            if (Discount.STATUS_APPROVED.equals(d.getStatus())) totalAmount = totalAmount.add(d.getAmount());
            if (Discount.STATUS_PENDING_APPROVAL.equals(d.getStatus())) pendingCount++;
        }

        return DiscountsReportResponse.builder().rows(rows).count(rows.size()).pendingCount(pendingCount).totalAmount(totalAmount).build();
    }

    public CancellationsReportResponse cancellations(LocalDate from, LocalDate to) {
        Long facilityId = SecurityUtils.requireFacilityId();
        List<Cancellation> cancellations = cancellationRepository.findByFacilityIdAndCreatedDateBetweenOrderByCreatedDateAsc(facilityId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        int approved = 0, rejected = 0, pending = 0;
        for (Cancellation c : cancellations) {
            if (Cancellation.STATUS_APPROVED.equals(c.getStatus())) approved++;
            else if (Cancellation.STATUS_REJECTED.equals(c.getStatus())) rejected++;
            else pending++;
        }

        return CancellationsReportResponse.builder()
                .rows(cancellations.stream().map(CancellationResponse::toResponse).toList())
                .totalCount(cancellations.size())
                .approvedCount(approved)
                .rejectedCount(rejected)
                .pendingCount(pending)
                .build();
    }

    /** How many OP patients in the date range also bought from this facility's pharmacy in the same window (§17.7 #12a). */
    public PharmacyConversionReportResponse pharmacyConversion(LocalDate from, LocalDate to) {
        Long facilityId = SecurityUtils.requireFacilityId();
        LocalDateTime fromTs = from.atStartOfDay();
        LocalDateTime toTs = to.plusDays(1).atStartOfDay();
        List<OpVisit> visits = opVisitRepository.findByFacilityIdAndArrivedTsBetweenOrderByArrivedTsAsc(facilityId, fromTs, toTs);

        Set<Long> patientIds = visits.stream().map(v -> v.getPatient().getId()).collect(Collectors.toSet());
        int converted = patientIds.isEmpty() ? 0
                : salesInvoiceRepository.findPatientIdsWithSaleInRange(facilityId, List.copyOf(patientIds), from, to).size();

        double pct = patientIds.isEmpty() ? 0.0 : (converted * 100.0) / patientIds.size();

        return PharmacyConversionReportResponse.builder()
                .totalVisits(visits.size())
                .distinctPatients(patientIds.size())
                .patientsWithPharmacySale(converted)
                .conversionPercent(Math.round(pct * 10) / 10.0)
                .build();
    }

    /** Free reviews (§7.4/§17.7 #12a) - "Free review — 2nd of 3, against consultation OP/2026/01432". */
    public FreeReviewsReportResponse freeReviews(LocalDate from, LocalDate to) {
        Long facilityId = SecurityUtils.requireFacilityId();
        List<FreeReviewLink> links = freeReviewLinkRepository
                .findByFacilityIdAndCreatedDateBetweenOrderByCreatedDateAsc(facilityId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        List<FreeReviewsReportRow> rows = new ArrayList<>();
        for (FreeReviewLink link : links) {
            List<Long> siblingIds = freeReviewLinkRepository.findByOriginalOpVisitIdOrderByCreatedDateAsc(link.getOriginalOpVisit().getId())
                    .stream().map(FreeReviewLink::getId).toList();
            int position = siblingIds.indexOf(link.getId()) + 1; // by id, not object identity - deterministic regardless of persistence-context caching
            int maxVisits = freeReviewPolicyRepository.findByDoctorIdAndFacilityId(link.getDoctor().getId(), facilityId)
                    .map(FreeReviewPolicy::getMaxVisits).orElse(position);

            rows.add(FreeReviewsReportRow.builder()
                    .patientName(link.getPatient().getFullName())
                    .patientMrn(link.getPatient().getMrn())
                    .doctorName(link.getDoctor().getFullName())
                    .visitedAt(link.getOpVisit().getArrivedTs())
                    .originalOpNo(link.getOriginalOpVisit().getOpNo())
                    .position(position)
                    .maxVisits(maxVisits)
                    .build());
        }

        return FreeReviewsReportResponse.builder().rows(rows).totalCount(rows.size()).build();
    }

    /** Commission (§8.1/§17.7 #12a) - see this class's own javadoc for the CommissionBasis name-matching caveat. */
    public CommissionReportResponse commission(LocalDate from, LocalDate to) {
        Long facilityId = SecurityUtils.requireFacilityId();
        List<OpVisit> visits = opVisitRepository.findByFacilityIdAndArrivedTsBetweenOrderByArrivedTsAsc(facilityId, from.atStartOfDay(), to.plusDays(1).atStartOfDay())
                .stream().filter(v -> v.getReferralDoctor() != null || v.getPro() != null).toList();

        List<CommissionBasis> activeBases = commissionBasisRepository.findByFacilityIdAndActiveTrue(facilityId);

        record PartyKey(String partyType, Long partyId, String partyName) {}
        Map<PartyKey, List<OpVisit>> byParty = new LinkedHashMap<>();
        for (OpVisit v : visits) {
            if (v.getReferralDoctor() != null) {
                byParty.computeIfAbsent(new PartyKey(CommissionBasis.PARTY_REFERRAL_DOCTOR, v.getReferralDoctor().getId(), v.getReferralDoctor().getName()), k -> new ArrayList<>()).add(v);
            }
            if (v.getPro() != null) {
                byParty.computeIfAbsent(new PartyKey(CommissionBasis.PARTY_PRO, v.getPro().getId(), v.getPro().getName()), k -> new ArrayList<>()).add(v);
            }
        }

        List<CommissionReportRow> rows = new ArrayList<>();
        BigDecimal totalCommission = BigDecimal.ZERO;
        for (Map.Entry<PartyKey, List<OpVisit>> entry : byParty.entrySet()) {
            PartyKey key = entry.getKey();
            BigDecimal totalBilled = BigDecimal.ZERO;
            for (OpVisit v : entry.getValue()) {
                Bill bill = billRepository.findByEncounterTypeAndEncounterId(Bill.ENCOUNTER_CONSULTATION, v.getId()).orElse(null);
                if (bill != null) totalBilled = totalBilled.add(bill.getGross());
            }

            CommissionBasis basis = activeBases.stream()
                    .filter(b -> b.getPartyType().equals(key.partyType()) && b.getPartyName().equalsIgnoreCase(key.partyName()))
                    .findFirst().orElse(null);

            BigDecimal commissionAmount = null;
            if (basis != null) {
                commissionAmount = CommissionBasis.BASIS_PERCENT.equals(basis.getBasis())
                        ? totalBilled.multiply(basis.getValue()).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP)
                        : basis.getValue().multiply(BigDecimal.valueOf(entry.getValue().size()));
                totalCommission = totalCommission.add(commissionAmount);
            }

            rows.add(CommissionReportRow.builder()
                    .partyType(key.partyType())
                    .partyName(key.partyName())
                    .visitCount(entry.getValue().size())
                    .totalBilled(totalBilled)
                    .basis(basis != null ? basis.getBasis() : null)
                    .basisValue(basis != null ? basis.getValue() : null)
                    .commissionAmount(commissionAmount)
                    .build());
        }

        return CommissionReportResponse.builder().rows(rows).totalCommission(totalCommission).build();
    }

    /** Area-wise consultations (§17.7 #12a, V47) - "Unspecified" for a patient with no area set, never silently dropped. */
    public AreaWiseConsultationsReportResponse areaWiseConsultations(LocalDate from, LocalDate to) {
        Long facilityId = SecurityUtils.requireFacilityId();
        List<OpVisit> visits = opVisitRepository.findByFacilityIdAndArrivedTsBetweenOrderByArrivedTsAsc(facilityId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        Map<String, List<Long>> patientIdsByArea = new LinkedHashMap<>();
        Map<String, Integer> visitCountByArea = new LinkedHashMap<>();
        for (OpVisit v : visits) {
            String label = v.getPatient().getArea() != null ? v.getPatient().getArea().displayLabel() : "Unspecified";
            patientIdsByArea.computeIfAbsent(label, k -> new ArrayList<>()).add(v.getPatient().getId());
            visitCountByArea.merge(label, 1, Integer::sum);
        }

        List<AreaWiseConsultationRow> rows = visitCountByArea.entrySet().stream()
                .map(e -> AreaWiseConsultationRow.builder()
                        .areaLabel(e.getKey())
                        .visitCount(e.getValue())
                        .distinctPatients((int) patientIdsByArea.get(e.getKey()).stream().distinct().count())
                        .build())
                .sorted((a, b) -> b.getVisitCount() - a.getVisitCount())
                .toList();

        return AreaWiseConsultationsReportResponse.builder().rows(rows).totalVisits(visits.size()).build();
    }
}
