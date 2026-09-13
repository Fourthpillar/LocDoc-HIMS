package com.lockdoc.doctor.dto;

import com.lockdoc.outpatient.entity.Appointment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** One appointment a proposed schedule block would affect - the surface §8.2 requires before the block can commit. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffectedAppointmentResponse {

    private Long appointmentId;
    private Long doctorScheduleId;
    private String sessionName;
    private String patientName;
    private String patientMrn;
    private LocalDateTime appointmentTs;
    private String status;

    public static AffectedAppointmentResponse toResponse(Appointment a) {
        return AffectedAppointmentResponse.builder()
                .appointmentId(a.getId())
                .doctorScheduleId(a.getDoctorSchedule().getId())
                .sessionName(a.getDoctorSchedule().getSessionName())
                .patientName(a.getPatient().getFullName())
                .patientMrn(a.getPatient().getMrn())
                .appointmentTs(a.getAppointmentTs())
                .status(a.getStatus())
                .build();
    }
}
