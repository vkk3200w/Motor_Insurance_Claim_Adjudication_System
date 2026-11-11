
package com.company.insurance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "DEPRECIATION_RULE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepreciationRule {

    @Id
    @Column(name = "rule_id")
    private Integer ruleId;

    @Column(name = "category", nullable = false, length = 10)
    private String category;

    @Column(name = "min_age", nullable = false)
    private Integer minAge;

    @Column(name = "max_age", nullable = false)
    private Integer maxAge;

    @Column(name = "dep_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal depPercent;
}
