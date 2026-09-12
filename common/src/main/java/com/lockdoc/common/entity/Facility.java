package com.lockdoc.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A hospital or clinic — the tenant every facility-scoped record belongs to.
 *
 * It lives in {@code common} rather than in a feature module because it is not
 * any one module's property: the doctor module scopes mappings, statuses, rates
 * and policies by it, and outpatient and pharmacy will scope their own records
 * by it as they grow. Putting it in a feature module would force the others to
 * reach across a module boundary for it, which the module rules forbid.
 *
 * A {@link User} may belong to one facility ({@code User.facility}); Super Admin
 * belongs to none, which is what makes them cross-facility.
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /** HOSPITAL or CLINIC — the two the doctor module distinguishes when a doctor picks where they practise. */
    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private String type = TYPE_CLINIC;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

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
