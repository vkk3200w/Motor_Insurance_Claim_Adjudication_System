
package com.company.insurance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CLAIM_FRAUD_FLAG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClaimFraudFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flag_id")
    private Long flagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private ClaimHeader claimHeader;

    @Column(name = "rule_code", nullable = false, length = 25)
    private String ruleCode;

    @Column(name = "points", nullable = false)
    private Integer points;
}
