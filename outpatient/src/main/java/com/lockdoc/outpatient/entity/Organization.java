package com.lockdoc.outpatient.entity;

import com.lockdoc.common.entity.Facility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Masters (Master Spec §7.2, §6, screen #34) - "missing entity, restored here": OpVisit.org_type and Payment.party_type used Organization/TPA as bare string values with no master data behind either, until now. */
@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {

    public static final String TYPE_ORGANIZATION = "ORGANIZATION";
    public static final String TYPE_TPA = "TPA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "org_type", nullable = false, length = 20)
    private String orgType;

    @Column(name = "contract_terms", length = 500)
    private String contractTerms;

    @Column(name = "credit_terms", length = 200)
    private String creditTerms;

    @Column(name = "authorisation_ref_format", length = 100)
    private String authorisationRefFormat;

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
