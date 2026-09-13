package com.lockdoc.outpatient.dto;

import com.lockdoc.outpatient.entity.AppointmentWaitlist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaitlistResponse {

    private Long id;
    private Long doctorScheduleId;
    private LocalDate sessionDate;
    private Long patientId;
    private String patientName;
    private String patientMrn;
    private LocalDateTime joinedAt;
    private LocalDateTime promotedAt;
    private Long promotedAppointmentId;

    public static WaitlistResponse toResponse(AppointmentWaitlist w) {
        return WaitlistResponse.builder()
                .id(w.getId())
                .doctorScheduleId(w.getDoctorSchedule().getId())
                .sessionDate(w.getSessionDate())
                .patientId(w.getPatient().getId())
                .patientName(w.getPatient().getFullName())
                .patientMrn(w.getPatient().getMrn())
                .joinedAt(w.getJoinedAt())
                .promotedAt(w.getPromotedAt())
                .promotedAppointmentId(w.getPromotedAppointmentId())
                .build();
    }
}
