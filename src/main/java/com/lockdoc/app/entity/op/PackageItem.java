package com.lockdoc.app.entity.op;

import jakarta.persistence.*;
import lombok.*;

/** One included item + quantity in a {@link Package} (Master Spec §6). */
@Entity
@Table(name = "package_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private Package packageEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billable_item_id", nullable = false)
    private BillableItem billableItem;

    @Column(name = "included_qty", nullable = false)
    private Integer includedQty;
}
