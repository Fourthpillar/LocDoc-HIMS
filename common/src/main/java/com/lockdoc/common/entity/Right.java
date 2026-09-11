package com.lockdoc.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rights")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Right {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "right_code", nullable = false, unique = true, length = 50)
    private String rightCode;

    @Column(name = "right_name", nullable = false, length = 100)
    private String rightName;

    @Column(name = "description", length = 255)
    private String description;
}
