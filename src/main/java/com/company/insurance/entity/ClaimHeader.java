
package com.company.insurance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "CLAIM_HEADER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClaimHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "claim_id")
    private Long claimId;

    @Column(name = "claim_ref", nullable = false, unique = true, length = 20)
    private String claimRef;

    @Column(name = "correlation_id", nullable = false, length = 36)
    private String correlationId;

    @Column(name = "source_system", nullable = false, length = 10)
    private String sourceSystem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_no", referencedColumnName = "policy_no", nullable = false)
    private Policy policy;

    @Column(name = "add_ons", length = 100)
    private String addOns;

    @Column(name = "submitted_at_utc", nullable = false)
    private LocalDateTime submittedAtUtc;

    @Column(name = "vehicle_reg_no", nullable = false, length = 12)
    private String vehicleRegNo;

    @Column(name = "vehicle_desc", length = 60)
    private String vehicleDesc;

    @Column(name = "vehicle_age_years", nullable = false)
    private Integer vehicleAgeYears;

    @Column(name = "claimant_name", nullable = false, length = 80)
    private String claimantName;

    @Column(name = "claimant_mobile_masked", nullable = false, length = 10)
    private String claimantMobileMasked;

    @Column(name = "claimant_email", length = 100)
    private String claimantEmail;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(name = "incident_time")
    private LocalTime incidentTime;

    @Column(name = "incident_type", nullable = false, length = 10)
    private String incidentType;

    @Column(name = "incident_location", nullable = false, length = 80)
    private String incidentLocation;

    @Column(name = "fir_number", length = 30)
    private String firNumber;

    @Column(name = "garage_code", nullable = false, length = 15)
    private String garageCode;

    @Column(name = "network_garage", nullable = false, length = 1)
    private String networkGarage;

    @Column(name = "total_assessed", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAssessed;

    @Column(name = "total_depreciation", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDepreciation;

    @Column(name = "deductible_applied", nullable = false, precision = 12, scale = 2)
    private BigDecimal deductibleApplied;

    @Column(name = "copay_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal copayAmount;

    @Column(name = "payable_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal payableAmount;

    @Column(name = "fraud_score", nullable = false)
    private Integer fraudScore;

    @Column(name = "status", nullable = false, length = 15)
    private String status;

    @Column(name = "status_reason", length = 200)
    private String statusReason;

    @Column(name = "payload_hash", nullable = false, length = 64)
    private String payloadHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
