package com.lockdoc.outpatient.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * The drawdown ledger for one included item of one {@link PackageSale}
 * (Master Spec §6 - "missing from every earlier revision, needed the
 * moment a package's included items are billed one at a time across
 * visits rather than all at once"). Created at sale time with
 * {@code remainingQty = includedQty}; {@code consume(qty)} is the only
 * mutation, enforced never to go negative.
 */
@Entity
@Table(name = "package_utilization")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageUtilization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_sale_id", nullable = false)
    private PackageSale packageSale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billable_item_id", nullable = false)
    private BillableItem billableItem;

    @Column(name = "included_qty", nullable = false)
    private Integer includedQty;

    @Column(name = "used_qty", nullable = false)
    @Builder.Default
    private Integer usedQty = 0;

    @Column(name = "remaining_qty", nullable = false)
    private Integer remainingQty;
}
