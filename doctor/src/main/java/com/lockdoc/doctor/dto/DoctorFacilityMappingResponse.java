package com.lockdoc.doctor.dto;

import com.lockdoc.common.entity.DoctorFacilityMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorFacilityMappingResponse {

    /** Monday-first, so hours read like a week rather than like the order they were typed. */
    private static final List<String> WEEKDAY_ORDER =
            List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");

    private Long id;
    private Long doctorId;
    private String doctorName;
    private String doctorRegistrationNumber;
    private Long facilityId;
    private String facilityName;
    private String relationshipType;
    private String status;
    /** FACILITY or DOCTOR — with status REQUESTED, this is what says which side still owes a response. */
    private String initiatedBy;
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;
    private LocalDateTime endedAt;
    /** What the doctor says their consulting hours here are (§8.2) — empty when they haven't stated any. */
    private List<ConsultationHourResponse> consultationHours;

    public static DoctorFacilityMappingResponse toResponse(DoctorFacilityMapping m) {
        return DoctorFacilityMappingResponse.builder()
                .id(m.getId())
                .doctorId(m.getDoctor().getId())
                .doctorName(m.getDoctor().getFullName())
                .doctorRegistrationNumber(m.getDoctor().getRegistrationNumber())
                .facilityId(m.getFacility().getId())
                .facilityName(m.getFacility().getName())
                .relationshipType(m.getRelationshipType())
                .status(m.getStatus())
                .initiatedBy(m.getInitiatedBy())
                .requestedAt(m.getRequestedAt())
                .respondedAt(m.getRespondedAt())
                .endedAt(m.getEndedAt())
                .consultationHours(m.getConsultationHours() == null ? Collections.emptyList()
                        : m.getConsultationHours().stream()
                                .sorted(Comparator.comparingInt((com.lockdoc.common.entity.DoctorFacilityMappingHour h) -> WEEKDAY_ORDER.indexOf(h.getWeekday()))
                                        .thenComparing(com.lockdoc.common.entity.DoctorFacilityMappingHour::getStartTime))
                                .map(ConsultationHourResponse::toResponse)
                                .toList())
                .build();
    }
}
