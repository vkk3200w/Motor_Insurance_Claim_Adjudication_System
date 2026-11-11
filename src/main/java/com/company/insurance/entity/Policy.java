
package com.company.insurance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "POLICY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Policy {

    @Id
    @Column(name = "policy_no", length = 20)
    private String policyNo;

    @Column(name = "product_code", nullable = false, length = 5)
    private String productCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "sum_insured", nullable = false, precision = 12, scale = 2)
    private BigDecimal sumInsured;

    @Column(name = "deductible", nullable = false, precision = 12, scale = 2)
    private BigDecimal deductible;

    @Column(name = "ncb_percent", nullable = false)
    private Integer ncbPercent;

    @Column(name = "status", nullable = false, length = 10)
    private String status;

    @Column(name = "last_claim_date")
    private LocalDate lastClaimDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;
}
