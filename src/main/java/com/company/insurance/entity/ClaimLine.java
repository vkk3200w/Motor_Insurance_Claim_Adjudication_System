
package com.company.insurance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
    name = "CLAIM_LINE",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_claim_line",
            columnNames = {"claim_id", "line_no"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClaimLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "claim_line_id")
    private Long claimLineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private ClaimHeader claimHeader;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    @Column(name = "part_code", nullable = false, length = 15)
    private String partCode;

    @Column(name = "category", nullable = false, length = 10)
    private String category;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    @Column(name = "dep_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal depPercent;

    @Column(name = "dep_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal depAmount;

    @Column(name = "approved_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "line_status", nullable = false, length = 10)
    private String lineStatus;

    @Column(name = "exclusion_reason", length = 40)
    private String exclusionReason;
}
