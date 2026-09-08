package com.lockdoc.app.entity.pharmacy;

import jakarta.persistence.*;
import lombok.*;

/**
 * Pure counter table - no surrogate id / created_date, since it is
 * only ever read-locked and incremented by DocumentNumberService.
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
