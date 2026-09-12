package com.lockdoc.app.dto.doctor;

import com.lockdoc.app.entity.doctor.DoctorFacilityMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * "When is Dr X in?" answered for the front desk (Master Spec §8.2).
 *
 * Reception already sees the facility's sessions (doctor_schedules) because
 * those are what they book into. This is the other half: what the doctor
 * themselves said they would be present for. The two usually agree, and when
 * they don't, the desk is the first to find out — which is why they get to
 * see both rather than only the version typed in by an admin.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorStatedHoursResponse {

    private static final List<String> WEEKDAY_ORDER =
            List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");

    private Long doctorId;
    private String doctorName;
    private String relationshipType;
    private List<ConsultationHourResponse> hours;

    public static DoctorStatedHoursResponse toResponse(DoctorFacilityMapping m) {
        return DoctorStatedHoursResponse.builder()
                .doctorId(m.getDoctor().getId())
                .doctorName(m.getDoctor().getFullName())
                .relationshipType(m.getRelationshipType())
                .hours(m.getConsultationHours() == null ? Collections.emptyList()
                        : m.getConsultationHours().stream()
                                .sorted(Comparator.comparingInt((com.lockdoc.app.entity.doctor.DoctorFacilityMappingHour h) -> WEEKDAY_ORDER.indexOf(h.getWeekday()))
                                        .thenComparing(com.lockdoc.app.entity.doctor.DoctorFacilityMappingHour::getStartTime))
                                .map(ConsultationHourResponse::toResponse)
                                .toList())
                .build();
    }
}
