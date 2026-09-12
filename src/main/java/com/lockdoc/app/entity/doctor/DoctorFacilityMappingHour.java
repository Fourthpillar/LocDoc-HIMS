package com.lockdoc.app.entity.doctor;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

/**
 * One weekday window a doctor says they will be consulting at a facility
 * (Master Spec §8.1/§8.2) — "Mondays, 10:00 to 13:00".
 *
 * This is the doctor's statement of intent, made when they ask to practise
 * somewhere (or updated afterwards), not the facility's session plan. The
 * facility turns these into {@link com.lockdoc.app.entity.op.DoctorSchedule}
 * rows, which add the parts only the facility can decide — how many patients
 * fit in the window, how far it may be overbooked, and from which date it
 * runs.
 */
@Entity
@Table(name = "doctor_facility_mapping_hours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorFacilityMappingHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mapping_id", nullable = false)
    private DoctorFacilityMapping mapping;

    /** MONDAY … SUNDAY, same vocabulary as DoctorSchedule.weekday so the two line up without translation. */
    @Column(name = "weekday", nullable = false, length = 10)
    private String weekday;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
}
