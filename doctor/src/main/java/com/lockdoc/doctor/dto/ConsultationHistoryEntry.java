package com.lockdoc.doctor.dto;

import com.lockdoc.doctor.entity.ConsultationNote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One prior, completed visit with this doctor (Master Spec §8.7/§8.9) —
 * read-only, collapsed-by-default summary. Opening the full note is a
 * separate GET by opVisitId; this is just enough to populate the list.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationHistoryEntry {

    private Long opVisitId;
    private LocalDateTime visitDate;
    private String provisionalDiagnosis;
    private String chiefComplaint;

    public static ConsultationHistoryEntry toEntry(ConsultationNote n) {
        return ConsultationHistoryEntry.builder()
                .opVisitId(n.getOpVisit().getId())
                .visitDate(n.getOpVisit().getArrivedTs())
                .provisionalDiagnosis(n.getProvisionalDiagnosis())
                .chiefComplaint(n.getChiefComplaint())
                .build();
    }
}
