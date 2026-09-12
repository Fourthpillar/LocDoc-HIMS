package com.lockdoc.doctor.dto;

import com.lockdoc.doctor.entity.DoctorStatus;
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
public class DoctorStatusResponse {

    private Long id;
    private Long doctorId;
    private String doctorName;
    private Long facilityId;
    private String facilityName;
    private LocalDate sessionDate;
    private String status;
    private String source;
    private Long setByUserId;
    private LocalDateTime createdDate;

    public static DoctorStatusResponse toResponse(DoctorStatus s) {
        return DoctorStatusResponse.builder()
                .id(s.getId())
                .doctorId(s.getDoctor().getId())
                .doctorName(s.getDoctor().getFullName())
                .facilityId(s.getFacility().getId())
                .facilityName(s.getFacility().getName())
                .sessionDate(s.getSessionDate())
                .status(s.getStatus())
                .source(s.getSource())
                .setByUserId(s.getSetByUserId())
                .createdDate(s.getCreatedDate())
                .build();
    }
}
