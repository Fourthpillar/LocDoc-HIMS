package com.lockdoc.doctor.service;

import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.doctor.dto.*;
import com.lockdoc.common.entity.Doctor;
import com.lockdoc.doctor.entity.ConsultationNote;
import com.lockdoc.doctor.entity.DoctorStatus;
import com.lockdoc.doctor.entity.Prescription;
import com.lockdoc.doctor.entity.PrescriptionLine;
import com.lockdoc.doctor.entity.ScheduleException;
import com.lockdoc.outpatient.entity.Bill;
import com.lockdoc.outpatient.entity.ConsultationRating;
import com.lockdoc.outpatient.entity.DoctorSchedule;
import com.lockdoc.outpatient.entity.OpVisit;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.common.repository.DoctorRepository;
import com.lockdoc.common.service.CurrentDoctorService;
import com.lockdoc.doctor.repository.ConsultationNoteRepository;
import com.lockdoc.doctor.repository.DoctorStatusRepository;
import com.lockdoc.doctor.repository.PrescriptionRepository;
import com.lockdoc.doctor.repository.ScheduleExceptionRepository;
import com.lockdoc.outpatient.repository.BillRepository;
import com.lockdoc.outpatient.repository.ConsultationRatingRepository;
import com.lockdoc.outpatient.repository.DoctorScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Doctor Reports (Master Spec §17.7 #18: punctuality, consultation count,
 * medicines prescribed) - doctor-facing, own data only.
 *
 * <p><b>Punctuality</b> is deliberately not the full {@code PunctualityScore}
 * rolling-score/advance-notice-weighting system §8.8 describes - that's
 * built on §8.4's GPS/ETA delay-cascade machinery, explicitly deferred to
 * the mobile-app phase (§16 step 10). What §8.3's status-only build
 * (already shipped) *does* give for free is a real, append-only log of
 * every status change with a timestamp - so this computes an honest,
 * simpler signal from that: for each scheduled session, the first
 * {@code AT_FACILITY}-or-later status change that day vs. the session's
 * scheduled start time. A day the doctor blocked via a
 * {@link ScheduleException} is excluded from delay math entirely (a
 * planned absence isn't lateness) rather than silently counted as a
 * no-show. One caveat worth stating rather than hiding: {@link DoctorStatus}
 * is logged per (doctor, facility, date), not per session, so a doctor
 * running two sessions at the same facility on the same day gets one
 * arrival timestamp compared against each session's own start time - the
 * data model doesn't carry finer granularity than that. Because of that, only
 * the day's first session at a facility can come out early; a later one with
 * the doctor already on site scores as on time (0 minutes), rather than as
 * hours early.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorReportsService {

    private static final List<String> ARRIVED_OR_LATER = List.of(
            DoctorStatus.AT_FACILITY, DoctorStatus.IN_CONSULTATION, DoctorStatus.IN_ROUNDS, DoctorStatus.DAY_COMPLETE);

    private final DoctorRepository doctorRepository;
    private final CurrentDoctorService currentDoctorService;
    private final DoctorScheduleRepository scheduleRepository;
    private final ScheduleExceptionRepository exceptionRepository;
    private final DoctorStatusRepository statusRepository;
    private final ConsultationNoteRepository consultationNoteRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final BillRepository billRepository;
    private final ConsultationRatingRepository ratingRepository;

    public PunctualityReportResponse punctuality(LocalDate from, LocalDate to, Long facilityIdFilter) {
        Doctor doctor = currentDoctor();

        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorIdAndActiveTrue(doctor.getId());
        if (facilityIdFilter != null) {
            schedules = schedules.stream().filter(s -> s.getFacility().getId().equals(facilityIdFilter)).toList();
        }

        List<ScheduleException> exceptions = exceptionRepository.findByDoctorIdAndExceptionDateBetweenOrderByExceptionDateAsc(doctor.getId(), from, to);

        // First AT_FACILITY-or-later status change per (facilityId, date).
        Map<String, LocalDateTime> firstArrival = new LinkedHashMap<>();
        for (DoctorStatus s : statusRepository.findByDoctorIdAndSessionDateBetweenOrderByCreatedDateAsc(doctor.getId(), from, to)) {
            if (!ARRIVED_OR_LATER.contains(s.getStatus())) continue;
            String key = s.getFacility().getId() + "|" + s.getSessionDate();
            firstArrival.putIfAbsent(key, s.getCreatedDate());
        }

        List<PunctualityRow> rows = new ArrayList<>();
        int onTime = 0, late = 0, noStatus = 0, blocked = 0;
        long delaySumMinutes = 0;
        int delaySamples = 0;

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            String weekday = date.getDayOfWeek().name();
            LocalDate d = date;
            List<DoctorSchedule> sessionsToday = schedules.stream()
                    .filter(s -> s.getWeekday().equals(weekday))
                    .filter(s -> !s.getEffectiveFrom().isAfter(d))
                    .filter(s -> s.getEffectiveTo() == null || !s.getEffectiveTo().isBefore(d))
                    .toList();

            // The first session each facility actually runs today - the only one a single per-day
            // arrival timestamp can honestly be "early" for.
            Map<Long, java.time.LocalTime> firstStartByFacility = new java.util.HashMap<>();
            for (DoctorSchedule s : sessionsToday) {
                if (isBlocked(exceptions, s, d)) continue;
                firstStartByFacility.merge(s.getFacility().getId(), s.getStartTime(), (a, b) -> a.isBefore(b) ? a : b);
            }

            for (DoctorSchedule s : sessionsToday) {
                boolean isBlocked = isBlocked(exceptions, s, d);

                if (isBlocked) {
                    blocked++;
                    rows.add(PunctualityRow.builder().date(d).facilityId(s.getFacility().getId()).facilityName(s.getFacility().getName())
                            .scheduledStart(s.getStartTime()).outcome("BLOCKED").build());
                    continue;
                }

                LocalDateTime arrivedAt = firstArrival.get(s.getFacility().getId() + "|" + d);
                if (arrivedAt == null) {
                    noStatus++;
                    rows.add(PunctualityRow.builder().date(d).facilityId(s.getFacility().getId()).facilityName(s.getFacility().getName())
                            .scheduledStart(s.getStartTime()).outcome("NO_STATUS").build());
                    continue;
                }

                long delayMin = Duration.between(d.atTime(s.getStartTime()), arrivedAt).toMinutes();
                // A later session the same day, with the doctor already on site: that's on time, not
                // "early" by however many hours sit between the two sessions - which otherwise swamps
                // the average (a 00:30 round then a 16:00 clinic read as 15 hours early).
                if (delayMin < 0 && s.getStartTime().isAfter(firstStartByFacility.get(s.getFacility().getId()))) {
                    delayMin = 0;
                }
                String outcome = delayMin <= 0 ? "ON_TIME" : "LATE";
                if ("ON_TIME".equals(outcome)) onTime++; else late++;
                delaySumMinutes += delayMin;
                delaySamples++;

                rows.add(PunctualityRow.builder().date(d).facilityId(s.getFacility().getId()).facilityName(s.getFacility().getName())
                        .scheduledStart(s.getStartTime()).arrivedAt(arrivedAt).delayMinutes((int) delayMin).outcome(outcome).build());
            }
        }
        rows.sort((a, b) -> {
            int byDate = a.getDate().compareTo(b.getDate());
            return byDate != 0 ? byDate : a.getScheduledStart().compareTo(b.getScheduledStart());
        });

        // Same "completed consultations in range" set the Consultation tab scores against - punctuality
        // and patient satisfaction read as two views of the same range, not two unrelated queries.
        List<ConsultationNote> ratedNotes = consultationNoteRepository.findCompletedInRange(doctor.getId(), from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        if (facilityIdFilter != null) {
            ratedNotes = ratedNotes.stream().filter(n -> n.getFacility().getId().equals(facilityIdFilter)).toList();
        }
        List<Long> ratedVisitIds = ratedNotes.stream().map(n -> n.getOpVisit().getId()).toList();
        RatingSummary ratingSummary = summarizeRatings(ratingsByVisitId(ratedVisitIds), ratedVisitIds.size());

        return PunctualityReportResponse.builder()
                .rows(rows)
                .sessionsWithStatus(onTime + late)
                .onTimeCount(onTime)
                .lateCount(late)
                .noStatusCount(noStatus)
                .blockedCount(blocked)
                .avgDelayMinutes(delaySamples == 0 ? null : Math.round((delaySumMinutes / (double) delaySamples) * 10) / 10.0)
                .ratingSummary(ratingSummary)
                .build();
    }

    /** Whether a leave/holiday/cancel/move on that date takes this session out of punctuality scoring - facility-wide when the exception names no session. */
    private static boolean isBlocked(List<ScheduleException> exceptions, DoctorSchedule s, LocalDate d) {
        return exceptions.stream().anyMatch(e -> e.getExceptionDate().equals(d)
                && (e.getDoctorSchedule() == null
                        ? e.getFacility().getId().equals(s.getFacility().getId())
                        : e.getDoctorSchedule().getId().equals(s.getId())));
    }

    /** Batched rating lookup for a set of OP visits - one query, keyed by visit id, for both per-row display and the range's aggregate. */
    private Map<Long, ConsultationRating> ratingsByVisitId(List<Long> opVisitIds) {
        if (opVisitIds.isEmpty()) return Map.of();
        return ratingRepository.findByOpVisitIdIn(opVisitIds).stream()
                .collect(java.util.stream.Collectors.toMap(r -> r.getOpVisit().getId(), r -> r));
    }

    private RatingSummary summarizeRatings(Map<Long, ConsultationRating> byVisitId, int totalEligible) {
        Map<Integer, Integer> distribution = new LinkedHashMap<>();
        for (int star = 1; star <= 5; star++) distribution.put(star, 0);
        int sum = 0;
        for (ConsultationRating r : byVisitId.values()) {
            distribution.merge(r.getRating(), 1, Integer::sum);
            sum += r.getRating();
        }
        Double average = byVisitId.isEmpty() ? null : Math.round((sum / (double) byVisitId.size()) * 10) / 10.0;
        return RatingSummary.builder()
                .average(average)
                .ratedCount(byVisitId.size())
                .totalEligible(totalEligible)
                .distribution(distribution)
                .build();
    }

    /** Net amount of this visit's CONSULTATION bill, or null if it was never billed or the bill was cancelled — a bill not yet raised isn't "$0 earned", it's unknown. */
    private BigDecimal consultationBillAmount(OpVisit visit) {
        return billRepository.findByEncounterTypeAndEncounterId(Bill.ENCOUNTER_CONSULTATION, visit.getId())
                .filter(b -> !Bill.STATUS_CANCELLED.equals(b.getStatus()))
                .map(Bill::getNet)
                .orElse(null);
    }

    public ConsultationCountReportResponse consultationCount(LocalDate from, LocalDate to, Long facilityIdFilter) {
        Doctor doctor = currentDoctor();
        List<ConsultationNote> notes = consultationNoteRepository.findCompletedInRange(doctor.getId(), from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        if (facilityIdFilter != null) {
            notes = notes.stream().filter(n -> n.getFacility().getId().equals(facilityIdFilter)).toList();
        }

        Map<Long, ConsultationRating> ratingsByVisit = ratingsByVisitId(notes.stream().map(n -> n.getOpVisit().getId()).toList());

        List<ConsultationCountRow> rows = notes.stream().map(n -> ConsultationCountRow.builder()
                .consultedAt(n.getCreatedDate())
                .facilityName(n.getFacility().getName())
                .patientName(n.getOpVisit().getPatient().getFullName())
                .patientMrn(n.getOpVisit().getPatient().getMrn())
                .visitType(n.getOpVisit().getVisitType())
                .billedAmount(consultationBillAmount(n.getOpVisit()))
                .rating(java.util.Optional.ofNullable(ratingsByVisit.get(n.getOpVisit().getId())).map(ConsultationRating::getRating).orElse(null))
                .build()).toList();

        Map<String, Integer> byFacilityCount = new LinkedHashMap<>();
        Map<String, BigDecimal> byFacilityAmount = new LinkedHashMap<>();
        for (ConsultationCountRow r : rows) {
            byFacilityCount.merge(r.getFacilityName(), 1, Integer::sum);
            if (r.getBilledAmount() != null) {
                byFacilityAmount.merge(r.getFacilityName(), r.getBilledAmount(), BigDecimal::add);
            }
        }
        List<FacilityCount> byFacility = byFacilityCount.entrySet().stream()
                .map(e -> FacilityCount.builder().facilityName(e.getKey()).count(e.getValue())
                        .amount(byFacilityAmount.getOrDefault(e.getKey(), BigDecimal.ZERO)).build())
                .toList();

        BigDecimal totalBilled = rows.stream().map(ConsultationCountRow::getBilledAmount).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ConsultationCountReportResponse.builder().rows(rows).totalCount(rows.size()).byFacility(byFacility).totalBilled(totalBilled)
                .ratingSummary(summarizeRatings(ratingsByVisit, rows.size())).build();
    }

    /**
     * Medicines prescribed (§17.7 #18). Beyond how often each medicine was
     * prescribed, this reports how the doctor usually writes it - the most
     * common strength, dosage, frequency, duration and route they entered.
     * Patients are counted once across the whole
     * range: summing the per-medicine counts would double-count anyone given
     * more than one medicine.
     */
    public MedicinesPrescribedReportResponse medicinesPrescribed(LocalDate from, LocalDate to, Long facilityIdFilter) {
        Doctor doctor = currentDoctor();
        List<Prescription> prescriptions = prescriptionRepository.findCompletedInRange(doctor.getId(), from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        if (facilityIdFilter != null) {
            prescriptions = prescriptions.stream().filter(p -> p.getFacility().getId().equals(facilityIdFilter)).toList();
        }

        Map<String, MedicineTally> tallies = new LinkedHashMap<>();
        java.util.Set<Long> allPatients = new java.util.HashSet<>();
        String granularity = trendGranularity(from, to);
        java.util.TreeMap<LocalDateTime, int[]> buckets = emptyTrendBuckets(from, to, granularity);
        int totalLines = 0;

        for (Prescription p : prescriptions) {
            Long patientId = p.getOpVisit().getPatient().getId();
            allPatients.add(patientId);
            int[] day = buckets.computeIfAbsent(trendBucketStart(p.getCreatedDate(), granularity, from), k -> new int[2]);
            day[0]++;

            for (PrescriptionLine line : p.getLines()) {
                totalLines++;
                day[1]++;
                MedicineTally t = tallies.computeIfAbsent(line.getMedicineName(), k -> new MedicineTally());
                t.times++;
                t.patients.add(patientId);
                MedicineTally.count(t.genericNames, line.getGenericName());
                MedicineTally.count(t.strengths, line.getStrength());
                MedicineTally.count(t.dosages, line.getDosage());
                MedicineTally.count(t.frequencies, line.getFrequency());
                MedicineTally.count(t.durations, line.getDuration());
                MedicineTally.count(t.routes, line.getRoute());
                if (line.getQuantity() != null) {
                    t.quantity += line.getQuantity();
                }
                if (t.lastPrescribedAt == null || p.getCreatedDate().isAfter(t.lastPrescribedAt)) {
                    t.lastPrescribedAt = p.getCreatedDate();
                }
            }
        }

        List<MedicinesPrescribedRow> rows = tallies.entrySet().stream()
                .map(e -> {
                    MedicineTally t = e.getValue();
                    return MedicinesPrescribedRow.builder()
                            .medicineName(e.getKey())
                            .timesPrescribed(t.times)
                            .distinctPatients(t.patients.size())
                            .genericName(mostCommon(t.genericNames))
                            .usualStrength(mostCommon(t.strengths))
                            .usualDosage(mostCommon(t.dosages))
                            .usualFrequency(mostCommon(t.frequencies))
                            .usualDuration(mostCommon(t.durations))
                            .usualRoute(mostCommon(t.routes))
                            .totalQuantity(t.quantity)
                            .lastPrescribedAt(t.lastPrescribedAt)
                            .build();
                })
                .sorted(java.util.Comparator.comparingInt(MedicinesPrescribedRow::getTimesPrescribed).reversed()
                        .thenComparing(MedicinesPrescribedRow::getMedicineName))
                .toList();

        List<PrescriptionTrendBucket> trend = buckets.entrySet().stream()
                .map(e -> PrescriptionTrendBucket.builder().start(e.getKey()).prescriptions(e.getValue()[0]).lines(e.getValue()[1]).build())
                .toList();

        return MedicinesPrescribedReportResponse.builder()
                .rows(rows)
                .totalPrescriptions(prescriptions.size())
                .totalLines(totalLines)
                .distinctPatients(allPatients.size())
                .trendGranularity(granularity)
                .trend(trend)
                .build();
    }

    static final String GRANULARITY_HOUR = "HOUR";
    static final String GRANULARITY_DAY = "DAY";
    static final String GRANULARITY_WEEK = "WEEK";
    static final String GRANULARITY_MONTH = "MONTH";

    /**
     * How finely the trend is bucketed, from the length of the range - so "This Year" is twelve monthly
     * bars and "Today" is hours, instead of every range drawing one bar per day.
     */
    private static String trendGranularity(LocalDate from, LocalDate to) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1;
        if (days <= 1) return GRANULARITY_HOUR;
        if (days <= 31) return GRANULARITY_DAY;
        if (days <= 120) return GRANULARITY_WEEK;
        return GRANULARITY_MONTH;
    }

    /**
     * Every bucket in the range, zero-filled and in order - a quiet stretch shows as a gap on the axis
     * instead of vanishing and making busy days look adjacent. The first week/month bucket starts at
     * the range start, not the calendar boundary, so it never reaches outside the dates picked.
     */
    private static java.util.TreeMap<LocalDateTime, int[]> emptyTrendBuckets(LocalDate from, LocalDate to, String granularity) {
        java.util.TreeMap<LocalDateTime, int[]> buckets = new java.util.TreeMap<>();
        switch (granularity) {
            case GRANULARITY_HOUR -> {
                for (int hour = 0; hour < 24; hour++) {
                    buckets.put(from.atTime(hour, 0), new int[2]);
                }
            }
            case GRANULARITY_DAY -> {
                for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                    buckets.put(d.atStartOfDay(), new int[2]);
                }
            }
            case GRANULARITY_WEEK -> {
                for (LocalDate d = from; !d.isAfter(to); d = d.with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.MONDAY))) {
                    buckets.put(d.atStartOfDay(), new int[2]);
                }
            }
            default -> {
                for (LocalDate d = from; !d.isAfter(to); d = d.withDayOfMonth(1).plusMonths(1)) {
                    buckets.put(d.atStartOfDay(), new int[2]);
                }
            }
        }
        return buckets;
    }

    /** The bucket a prescription falls in - clipped to the range start for the first week/month, matching emptyTrendBuckets. */
    private static LocalDateTime trendBucketStart(LocalDateTime at, String granularity, LocalDate from) {
        LocalDate day = at.toLocalDate();
        return switch (granularity) {
            case GRANULARITY_HOUR -> at.truncatedTo(java.time.temporal.ChronoUnit.HOURS);
            case GRANULARITY_DAY -> day.atStartOfDay();
            case GRANULARITY_WEEK -> {
                LocalDate monday = day.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                yield (monday.isBefore(from) ? from : monday).atStartOfDay();
            }
            default -> {
                LocalDate first = day.withDayOfMonth(1);
                yield (first.isBefore(from) ? from : first).atStartOfDay();
            }
        };
    }

    /** Per-medicine accumulator for the medicines report — every free-text value entered is counted, so the usual one can be picked. */
    private static final class MedicineTally {
        int times;
        int quantity;
        LocalDateTime lastPrescribedAt;
        final java.util.Set<Long> patients = new java.util.HashSet<>();
        final Map<String, Integer> genericNames = new LinkedHashMap<>();
        final Map<String, Integer> strengths = new LinkedHashMap<>();
        final Map<String, Integer> dosages = new LinkedHashMap<>();
        final Map<String, Integer> frequencies = new LinkedHashMap<>();
        final Map<String, Integer> durations = new LinkedHashMap<>();
        final Map<String, Integer> routes = new LinkedHashMap<>();

        static void count(Map<String, Integer> counts, String value) {
            if (value != null && !value.isBlank()) {
                counts.merge(value.trim(), 1, Integer::sum);
            }
        }
    }

    /** The value entered most often; a tie goes to whichever was entered first. Null when the field was never filled in. */
    private static String mostCommon(Map<String, Integer> counts) {
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private Doctor currentDoctor() {
        return currentDoctorService.require();
    }
}
