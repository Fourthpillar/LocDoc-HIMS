package com.lockdoc.app.entity.op;

import com.lockdoc.app.entity.Facility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Masters (Master Spec §7.2, §6, screen #34) - "missing entity, restored here": Public Relations Officer, referenced by CommissionBasis's party_type=PRO but never itself defined as a table until now. */
@Entity
@Table(name = "pros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "contact", length = 100)
    private String contact;

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
}
