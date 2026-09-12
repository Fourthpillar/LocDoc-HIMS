package com.lockdoc.app.entity.op;

import com.lockdoc.app.entity.Facility;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * The "procedure/service charge masters" (Master Spec §7.2) - one table
 * for both Service and Procedure, not two: §7.2 already names them
 * together ("on the same org-type dimensions") and {@link Bill} has a
 * single {@code PROCEDURE} encounter type for both, so keeping them
 * structurally identical rows with an {@link #itemType} discriminator
 * avoids duplicating the same shape twice - the same call CounterSession
 * (V37) made for OP/Pharmacy tills.
 */
@Entity
@Table(name = "billable_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillableItem {

    public static final String TYPE_SERVICE = "SERVICE";
    public static final String TYPE_PROCEDURE = "PROCEDURE";

    public static final String ORG_TYPE_DIRECT = "DIRECT";
    public static final String ORG_TYPE_ORGANIZATION = "ORGANIZATION";
    public static final String ORG_TYPE_TPA = "TPA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "item_type", nullable = false, length = 10)
    private String itemType;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", length = 30)
    private String code;

    @Column(name = "rate_direct", nullable = false, precision = 10, scale = 2)
    private BigDecimal rateDirect;

    /** Null falls back to rateDirect - not every facility prices Organization/TPA differently from every item. */
    @Column(name = "rate_organization", precision = 10, scale = 2)
    private BigDecimal rateOrganization;

    @Column(name = "rate_tpa", precision = 10, scale = 2)
    private BigDecimal rateTpa;

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

    /** Resolves the payable rate for an org type, falling back to the Direct rate when Organization/TPA isn't priced separately. */
    public BigDecimal rateFor(String orgType) {
        if (ORG_TYPE_ORGANIZATION.equals(orgType) && rateOrganization != null) return rateOrganization;
        if (ORG_TYPE_TPA.equals(orgType) && rateTpa != null) return rateTpa;
        return rateDirect;
    }
}
