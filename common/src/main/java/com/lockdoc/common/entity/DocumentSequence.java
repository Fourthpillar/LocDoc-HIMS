package com.lockdoc.common.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Pure counter table - no surrogate id / created_date, since it is
 * only ever read-locked and incremented by DocumentNumberService.
 *
 * Number series are per-facility, per-financial-year, gapless (Master Spec
 * §6) - facilityId joined the composite key in V9 so two facilities never
 * share a counter (and, not incidentally, two facilities' document numbers
 * no longer collide now that PO/GRN/invoice numbers are unique per facility
 * rather than globally - see the entities that generate them).
 */
@Entity
@Table(name = "document_sequences")
@IdClass(DocumentSequenceId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSequence {

    @Id
    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @Id
    @Column(name = "doc_type", nullable = false, length = 30)
    private String docType;

    @Id
    @Column(name = "seq_year", nullable = false)
    private Integer year;

    @Column(name = "prefix", nullable = false, length = 10)
    private String prefix;

    @Column(name = "last_number", nullable = false)
    @Builder.Default
    private Integer lastNumber = 0;
}
