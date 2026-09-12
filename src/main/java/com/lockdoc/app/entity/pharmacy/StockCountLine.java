package com.lockdoc.app.entity.pharmacy;

import jakarta.persistence.*;
import lombok.*;

/**
 * "Blind" means the counting UI doesn't surface {@code systemQty} while
 * counting - it's still captured at creation so a variance can be
 * computed the moment counted quantities come back (Master Spec §11.4).
 */
@Entity
@Table(name = "stock_count_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockCountLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_count_id", nullable = false)
    private StockCount stockCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_batch_id", nullable = false)
    private MedicineBatch medicineBatch;

    @Column(name = "system_qty", nullable = false)
    private Integer systemQty;

    /** Null until the count is actually performed. */
    @Column(name = "counted_qty")
    private Integer countedQty;

    public Integer getVariance() {
        return countedQty == null ? null : countedQty - systemQty;
    }
}
