package com.lockdoc.outpatient.entity;

import com.lockdoc.common.entity.Facility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One Patient row per facility
    // (Master Spec §6 invariant 7) - not global across facilities.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    // Was globally unique (see V3); now unique per facility - see V9.
    @Column(name = "mrn", nullable = false, length = 30)
    private String mrn;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address", length = 300)
    private String address;

    /** Area-wise consultations report (§17.7 #12a, V47) - nullable, existing patients aren't force-backfilled. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private com.lockdoc.outpatient.entity.Area area;

    // Free-text, comma-separated - read by the OP Module's PatientHeaderBar
    // allergy banner (Master Spec §17.6).
    @Column(name = "allergies", length = 500)
    private String allergies;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdDate = now;
        this.updatedDate = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
