package com.lockdoc.app.entity.pharmacy;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "store_transfer_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreTransferItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_transfer_id", nullable = false)
    private StoreTransfer storeTransfer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "batch_no", nullable = false, length = 50)
    private String batchNo;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "issued_qty", nullable = false)
    private Integer issuedQty;

    /** Null until received - discrepancy = issuedQty - receivedQty, captured on receipt (§11.4). */
    @Column(name = "received_qty")
    private Integer receivedQty;
}
