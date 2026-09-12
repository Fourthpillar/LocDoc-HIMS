package com.lockdoc.app.entity.pharmacy;

import com.lockdoc.app.entity.Facility;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The catalogue itself is facility-level (shared across a facility's
    // stores) - physical stock per store lives on MedicineBatch instead.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    // Was globally unique (see V3); now unique per facility - see V9.
    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "generic_name", length = 200)
    private String genericName;

    @Column(name = "manufacturer", length = 150)
    private String manufacturer;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "uom", nullable = false, length = 20)
    private String uom;

    @Column(name = "hsn_code", length = 20)
    private String hsnCode;

    @Column(name = "tax_percent", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxPercent = BigDecimal.ZERO;

    @Column(name = "reorder_level", nullable = false)
    @Builder.Default
    private Integer reorderLevel = 0;

    @Column(name = "is_schedule_drug", nullable = false)
    @Builder.Default
    private Boolean isScheduleDrug = false;

    public static final String SCHEDULE_OTC = "OTC";
    public static final String SCHEDULE_H = "H";
    public static final String SCHEDULE_H1 = "H1";
    public static final String SCHEDULE_X = "X";
    public static final String SCHEDULE_NARCOTIC = "NARCOTIC";

    /**
     * Drives mandatory prescriber capture at dispensing (Master Spec
     * §11.5) and, later, StatutoryRegisterEntry (§6/§15.2) - kept
     * alongside {@link #isScheduleDrug} rather than replacing it, since
     * that flag is already wired into MedicineService/DTOs.
     */
    @Column(name = "drug_schedule", length = 10)
    private String drugSchedule;

    /** Triggers the dispensing-time confirmation step (Master Spec §11.5) - a soft-stop, not a hard block. */
    @Column(name = "high_alert", nullable = false)
    @Builder.Default
    private Boolean highAlert = false;

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

    /** H/H1/X/Narcotic require the prescriber's name+registration number captured at dispensing (§11.5); OTC/unset don't. */
    public boolean requiresPrescriberCapture() {
        return SCHEDULE_H.equals(drugSchedule) || SCHEDULE_H1.equals(drugSchedule)
                || SCHEDULE_X.equals(drugSchedule) || SCHEDULE_NARCOTIC.equals(drugSchedule);
    }
}
