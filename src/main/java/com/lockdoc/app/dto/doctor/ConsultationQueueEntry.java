package com.lockdoc.app.dto.doctor;

import com.lockdoc.app.entity.op.OpVisit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Today's visits waiting on this doctor (Master Spec §17.6 — ConsultationWorkspace's own queue). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationQueueEntry {

    private Long opVisitId;
    private String opNo;
    private Long facilityId;
    private String facilityName;
    private Long patientId;
    private String patientName;
    private String patientMrn;
    private String visitStatus;
    private LocalDateTime arrivedTs;
    private Boolean hasDraftNote;
    /** New/Follow-up/... (§7.4) - the queue's own "known patient" signal: anything but NEW means this doctor has seen them before. */
    private String visitType;
    /** Casualty/medico-legal (§7.4) - the queue's own "needs priority attention" signal; there's no separate manual priority flag in this build. */
    private Boolean mlcFlag;

    public static ConsultationQueueEntry toEntry(OpVisit v, boolean hasDraftNote) {
        return ConsultationQueueEntry.builder()
                .opVisitId(v.getId())
                .opNo(v.getOpNo())
                .facilityId(v.getFacility().getId())
                .facilityName(v.getFacility().getName())
                .patientId(v.getPatient().getId())
                .patientName(v.getPatient().getFullName())
                .patientMrn(v.getPatient().getMrn())
                .visitStatus(v.getStatus())
                .arrivedTs(v.getArrivedTs())
                .hasDraftNote(hasDraftNote)
                .visitType(v.getVisitType())
                .mlcFlag(v.getMlcFlag())
                .build();
    }
}
