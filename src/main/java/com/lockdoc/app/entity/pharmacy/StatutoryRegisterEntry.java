package com.lockdoc.app.entity.pharmacy;

import com.lockdoc.app.entity.Facility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The entity behind §15.2's "build at launch" decision (Master Spec §6)
 * - auto-created by SalesService whenever a posted sale line's medicine
 * is a scheduled drug (H/H1/X/Narcotic), reusing exactly the prescriber
 * capture build order step 6 already made mandatory at the point of sale.
 */
@Entity
@Table(name = "statutory_register_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatutoryRegisterEntry {

    public static final String TYPE_SCH_H = "SCH_H";
    public static final String TYPE_SCH_H1 = "SCH_H1";
    public static final String TYPE_NDPS = "NDPS";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "register_type", nullable = false, length = 10)
    private String registerType;

    @Column(name = "sales_invoice_item_id", nullable = false, unique = true)
    private Long salesInvoiceItemId;

    @Column(name = "medicine_name", nullable = false, length = 200)
    private String medicineName;

    @Column(name = "patient_name", length = 150)
    private String patientName;

    @Column(name = "qty", nullable = false)
    private Integer qty;

    @Column(name = "prescriber_name", nullable = false, length = 150)
    private String prescriberName;

    @Column(name = "prescriber_registration_number", nullable = false, length = 100)
    private String prescriberRegistrationNumber;

    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    /** H maps to Schedule H's register, H1/X to Schedule H1's, NARCOTIC to the NDPS register. */
    public static String registerTypeFor(String drugSchedule) {
        if (Medicine.SCHEDULE_NARCOTIC.equals(drugSchedule)) return TYPE_NDPS;
        if (Medicine.SCHEDULE_H1.equals(drugSchedule) || Medicine.SCHEDULE_X.equals(drugSchedule)) return TYPE_SCH_H1;
        return TYPE_SCH_H;
    }
}
