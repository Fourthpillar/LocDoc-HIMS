package com.lockdoc.doctor.entity;

import com.lockdoc.common.entity.Facility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The relationship between a doctor and a facility they practise at
 * (Master Spec §8.1/§6) — a doctor's single identity ({@link Doctor},
 * never re-verified per facility) can hold several of these
 * concurrently, one per facility.
 *
 * Two-sided confirmation, openable from either side: a Hospital/Clinic
 * Admin can invite a verified doctor, and a verified doctor can request a
 * facility. Whichever side opens it, the other side accepts or declines —
 * {@link #initiatedBy} is what says which of those two situations a
 * REQUESTED row is in, and is what stops a doctor from approving their own
 * request (or an admin their own invite).
 *
 * Cabin/commission terms are deliberately not modeled here yet — cabin
 * rental is out of scope until build order step 10 (§15.1).
 */
@Entity
@Table(name = "doctor_facility_mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorFacilityMapping {

    public static final String RELATIONSHIP_OWNS_ONLY = "OWNS_ONLY";
    public static final String RELATIONSHIP_OWNS_AND_VISITS = "OWNS_AND_VISITS";
    public static final String RELATIONSHIP_VISITS_ONLY = "VISITS_ONLY";

    public static final String STATUS_REQUESTED = "REQUESTED";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_DECLINED = "DECLINED";
    public static final String STATUS_ENDED = "ENDED";

    /** The facility invited the doctor — REQUESTED means the doctor owes a response. */
    public static final String INITIATED_BY_FACILITY = "FACILITY";
    /** The doctor asked to practise here — REQUESTED means the facility owes a response. */
    public static final String INITIATED_BY_DOCTOR = "DOCTOR";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "relationship_type", nullable = false, length = 20)
    private String relationshipType;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = STATUS_REQUESTED;

    /** Which side opened this relationship — see the class javadoc for why REQUESTED is ambiguous without it. */
    @Column(name = "initiated_by", nullable = false, length = 20)
    @Builder.Default
    private String initiatedBy = INITIATED_BY_FACILITY;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    /**
     * When the doctor says they will be consulting here (§8.2). Stated with the
     * request so the facility can answer it knowing the hours, and kept afterwards
     * as what reception sees when asked "when is this doctor in?".
     */
    @OneToMany(mappedBy = "mapping", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DoctorFacilityMappingHour> consultationHours = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.requestedAt == null) {
            this.requestedAt = LocalDateTime.now();
        }
    }
}
