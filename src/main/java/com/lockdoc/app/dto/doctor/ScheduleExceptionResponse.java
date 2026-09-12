package com.lockdoc.app.dto.doctor;

import com.lockdoc.app.entity.doctor.ScheduleException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleExceptionResponse {

    private Long id;
    private Long doctorId;
    private String doctorName;
    private Long facilityId;
    private String facilityName;
    private Long doctorScheduleId;
    private String sessionName;
    private LocalDate exceptionDate;
    private String type;
    private String reason;
    /** Set only when type is SESSION_MOVE. */
    private LocalTime newStartTime;
    private LocalTime newEndTime;
    private LocalDateTime createdDate;

    public static ScheduleExceptionResponse toResponse(ScheduleException e) {
        return ScheduleExceptionResponse.builder()
                .id(e.getId())
                .doctorId(e.getDoctor().getId())
                .doctorName(e.getDoctor().getFullName())
                .facilityId(e.getFacility().getId())
                .facilityName(e.getFacility().getName())
                .doctorScheduleId(e.getDoctorSchedule() != null ? e.getDoctorSchedule().getId() : null)
                .sessionName(e.getDoctorSchedule() != null ? e.getDoctorSchedule().getSessionName() : "Whole day")
                .exceptionDate(e.getExceptionDate())
                .type(e.getExceptionType())
                .reason(e.getReason())
                .newStartTime(e.getNewStartTime())
                .newEndTime(e.getNewEndTime())
                .createdDate(e.getCreatedDate())
                .build();
    }
}
