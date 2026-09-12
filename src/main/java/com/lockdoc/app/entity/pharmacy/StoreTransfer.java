package com.lockdoc.app.entity.pharmacy;

import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Issue -> in-transit -> receipt, with discrepancy captured on receipt (Master Spec §6/§11.4). */
@Entity
@Table(name = "store_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreTransfer {

    public static final String STATUS_IN_TRANSIT = "IN_TRANSIT";
    public static final String STATUS_RECEIVED = "RECEIVED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "transfer_no", nullable = false, length = 30)
    private String transferNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_store_id", nullable = false)
    private Store fromStore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_store_id", nullable = false)
    private Store toStore;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = STATUS_IN_TRANSIT;

    @Column(name = "issued_by_user_id", nullable = false)
    private Long issuedByUserId;

    @Column(name = "received_by_user_id")
    private Long receivedByUserId;

    @Column(name = "issued_date", nullable = false)
    private LocalDateTime issuedDate;

    @Column(name = "received_date")
    private LocalDateTime receivedDate;

    @Column(name = "discrepancy_notes", length = 500)
    private String discrepancyNotes;

    @OneToMany(mappedBy = "storeTransfer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StoreTransferItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.issuedDate == null) {
            this.issuedDate = LocalDateTime.now();
        }
    }
}
