package com.lockdoc.outpatient.dto;

import com.lockdoc.outpatient.entity.DoctorSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorScheduleResponse {

    private Long id;
    private Long doctorId;
    private String doctorName;
    private Long facilityId;
    private String weekday;
    private String sessionName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer capacity;
    private Integer overbookAllowance;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean active;

    public static DoctorScheduleResponse toResponse(DoctorSchedule s) {
        return DoctorScheduleResponse.builder()
                .id(s.getId())
                .doctorId(s.getDoctor().getId())
                .doctorName(s.getDoctor().getFullName())
                .facilityId(s.getFacility().getId())
                .weekday(s.getWeekday())
                .sessionName(s.getSessionName())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .capacity(s.getCapacity())
                .overbookAllowance(s.getOverbookAllowance())
                .effectiveFrom(s.getEffectiveFrom())
                .effectiveTo(s.getEffectiveTo())
                .active(s.getActive())
                .build();
    }
}
