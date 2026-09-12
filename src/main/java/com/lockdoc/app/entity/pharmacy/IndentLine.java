package com.lockdoc.app.entity.pharmacy;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "indent_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndentLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_id", nullable = false)
    private Indent indent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "requested_qty", nullable = false)
    private Integer requestedQty;

    /** Null until approved - Hospital/Clinic Admin may approve less than requested. */
    @Column(name = "approved_qty")
    private Integer approvedQty;

    @Column(name = "issued_qty", nullable = false)
    @Builder.Default
    private Integer issuedQty = 0;
}
