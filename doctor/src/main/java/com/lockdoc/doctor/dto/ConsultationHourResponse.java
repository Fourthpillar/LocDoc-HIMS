package com.lockdoc.doctor.dto;

import com.lockdoc.doctor.entity.DoctorFacilityMappingHour;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationHourResponse {

    private Long id;
    private String weekday;
    private LocalTime startTime;
    private LocalTime endTime;

    public static ConsultationHourResponse toResponse(DoctorFacilityMappingHour h) {
        return ConsultationHourResponse.builder()
                .id(h.getId())
                .weekday(h.getWeekday())
                .startTime(h.getStartTime())
                .endTime(h.getEndTime())
                .build();
    }
}
