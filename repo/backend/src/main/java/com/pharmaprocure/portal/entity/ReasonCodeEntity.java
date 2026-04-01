package com.pharmaprocure.portal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reason_codes")
@Getter
@Setter
public class ReasonCodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_type", nullable = false, length = 40)
    private String codeType;

    @Column(nullable = false, length = 60)
    private String code;

    @Column(nullable = false, length = 160)
    private String label;

    @Column(nullable = false)
    private boolean active;
}
