package com.lockdoc.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * A tenant - a hospital, clinic, pharmacy or lab onboarded onto the
 * platform. Every fact in the system that isn't platform-level reference
 * data (roles/rights) belongs to exactly one Facility - see Master Spec
 * &sect;5 principle 1 ("multi-tenant from the data model up").
 *
 * {@code activeModules} is the module-entitlement set referenced throughout
 * the spec (OP / DOCTOR / PHARMACY / LABS) - what a facility can see and
 * transact against, independent of what UI exists (&sect;5 principle 2).
 */
@Entity
@Table(name = "facilities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facility {

    public static final String TYPE_HOSPITAL = "HOSPITAL";
    public static final String TYPE_CLINIC = "CLINIC";
    public static final String TYPE_PHARMACY = "PHARMACY";
    public static final String TYPE_LAB = "LAB";

    public static final String VERIFICATION_PENDING = "PENDING";
    public static final String VERIFICATION_VERIFIED = "VERIFIED";
    public static final String VERIFICATION_REJECTED = "REJECTED";
    public static final String VERIFICATION_SUSPENDED = "SUSPENDED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "geo_lat", precision = 10, scale = 7)
    private BigDecimal geoLat;

    @Column(name = "geo_lng", precision = 10, scale = 7)
    private BigDecimal geoLng;

    @Column(name = "licence_number", length = 100)
    private String licenceNumber;

    @Column(name = "verification_status", nullable = false, length = 20)
    @Builder.Default
    private String verificationStatus = VERIFICATION_PENDING;

    // Manual suspend/reinstate (Master Spec §10) is distinct from
    // verificationStatus - a VERIFIED facility can still be suspended for an
    // operational reason (e.g. a licence issue surfacing post-onboarding).
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "facility_modules", joinColumns = @JoinColumn(name = "facility_id"))
    @Column(name = "module_code", length = 20)
    @Builder.Default
    private Set<String> activeModules = new HashSet<>();

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
