package com.lockdoc.outpatient.dto;

import com.lockdoc.outpatient.entity.Appointment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {

    private Long id;
    private Long facilityId;
    private Long doctorId;
    private String doctorName;
    private Long doctorScheduleId;
    private String sessionName;
    private Long patientId;
    private String patientName;
    private String patientMrn;
    private LocalDateTime appointmentTs;
    private String status;
    /** CONSULTATION or PROCEDURE — what the slot was booked for (§7.5). */
    private String purpose;
    private String channel;
    private String cancelReason;

    public static AppointmentResponse toResponse(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .facilityId(a.getFacility().getId())
                .doctorId(a.getDoctor().getId())
                .doctorName(a.getDoctor().getFullName())
                .doctorScheduleId(a.getDoctorSchedule().getId())
                .sessionName(a.getDoctorSchedule().getSessionName())
                .patientId(a.getPatient().getId())
                .patientName(a.getPatient().getFullName())
                .patientMrn(a.getPatient().getMrn())
                .appointmentTs(a.getAppointmentTs())
                .status(a.getStatus())
                .purpose(a.getPurpose())
                .channel(a.getChannel())
                .cancelReason(a.getCancelReason())
                .build();
    }
}
