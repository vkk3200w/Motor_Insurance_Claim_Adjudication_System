
package com.company.insurance.service;

import com.company.insurance.dto.request.ClaimRequest;
import com.company.insurance.dto.request.LineItemRequest;
import com.company.insurance.dto.response.ClaimResponse;

import com.company.insurance.entity.*;
import com.company.insurance.exception.PolicyNotFoundException;

import com.company.insurance.repository.*;
import com.company.insurance.rules.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Service
public class ClaimAdjudicationService {

    private final PolicyRepository policyRepository;
    private final DepreciationRuleRepository depreciationRuleRepository;
    private final ClaimHeaderRepository claimHeaderRepository;
    private final ClaimLineRepository claimLineRepository;
    private final ClaimFraudFlagRepository claimFraudFlagRepository;
    private final ClaimAuditRepository claimAuditRepository;
    private final DepreciationRuleEngine depreciationRuleEngine;
    private final FraudDetectionRule fraudDetectionRule;

    public ClaimAdjudicationService(
            PolicyRepository policyRepository,
            DepreciationRuleRepository depreciationRuleRepository,
            ClaimHeaderRepository claimHeaderRepository,
            ClaimLineRepository claimLineRepository,
            ClaimFraudFlagRepository claimFraudFlagRepository,
            ClaimAuditRepository claimAuditRepository,
            DepreciationRuleEngine depreciationRuleEngine,
            FraudDetectionRule fraudDetectionRule) {

        this.policyRepository = policyRepository;
        this.depreciationRuleRepository = depreciationRuleRepository;
        this.claimHeaderRepository = claimHeaderRepository;
        this.claimLineRepository = claimLineRepository;
        this.claimFraudFlagRepository = claimFraudFlagRepository;
        this.claimAuditRepository = claimAuditRepository;
        this.depreciationRuleEngine = depreciationRuleEngine;
        this.fraudDetectionRule = fraudDetectionRule;
    }
    public boolean isIdenticalReplay(String claimRef, String rawRequestBody) {
    String payloadHash = calculatePayloadHash(rawRequestBody);

    return claimHeaderRepository.findByClaimRef(claimRef)
            .map(existing -> payloadHash.equals(existing.getPayloadHash()))
            .orElse(false);
}

    public Policy findPolicyByNumber(String policyNo) {
        return policyRepository.findById(policyNo)
                .orElseThrow(() -> new PolicyNotFoundException(policyNo));
    }

    private String calculatePayloadHash(String rawRequestBody) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(
                    rawRequestBody.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable", e);
        }
    }

    // R1
    public NormalizedClaimData normalizeClaim(ClaimRequest request) {
        return ClaimDataTransformer.transform(request);
    }

    // R2
    public String checkPolicyCoverage(ClaimRequest request) {
        Policy policy = findPolicyByNumber(
                request.getPolicy().getPolicyNo());

        return PolicyCoverageRule.checkCoverage(
                policy,
                request.getIncident().getDate());
    }

    // R3
    public LineCalculation calculateLineItem(LineItemRequest item) {
        return LineCalculationRule.calculate(
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTaxPercent());
    }

    // R4
    public BigDecimal getDepreciationPercent(
            String category,
            int vehicleAge,
            boolean zeroDep) {

        return depreciationRuleEngine.getDepreciationPercent(
                category, vehicleAge, zeroDep);
    }

    public DepreciationCalculation calculateDepreciation(
            BigDecimal lineTotal,
            BigDecimal depPercent) {

        return depreciationRuleEngine.calculateDepreciation(
                lineTotal, depPercent);
    }

    // R5
    public boolean shouldExcludeFloodEnginePart(
            String incidentType,
            String partCode,
            List<String> addOns) {

        boolean engineProtect = addOns != null
                && addOns.contains("ENGINE_PROTECT");

        return SpecialClaimRule.shouldExcludeFloodEnginePart(
                incidentType, partCode, engineProtect);
    }

    // R7 - Fraud points
    private int getFraudPoints(String ruleCode) {
        return switch (ruleCode) {
            case "F01" -> 30;
            case "F02" -> 40;
            case "F03" -> 25;
            case "F04" -> 15;
            case "F05" -> 10;
            case "F06" -> 10;
            default -> 0;
        };
    }


    @Transactional
    public ClaimResponse processClaim(
            ClaimRequest request,
            String correlationId,
            String sourceSystem,
            String rawRequestBody) {

        // R1 - Normalize claim
        NormalizedClaimData normalized = normalizeClaim(request);

        // R2 - Find policy and check coverage
        Policy policy = findPolicyByNumber(
                request.getPolicy().getPolicyNo());

        String coverageReason = PolicyCoverageRule.checkCoverage(
                policy,
                request.getIncident().getDate());

        // Idempotency - calculate request hash
        String payloadHash = calculatePayloadHash(rawRequestBody);

        var existingClaimOptional =
                claimHeaderRepository.findByClaimRef(request.getClaimRef());

        ClaimHeader existingClaim = existingClaimOptional.orElse(null);

        // Same request: return previously stored result
        if (existingClaim != null
                && existingClaim.getPayloadHash().equals(payloadHash)) {

            List<String> existingFlags =
                    claimFraudFlagRepository
                            .findByClaimHeaderClaimId(existingClaim.getClaimId())
                            .stream()
                            .map(ClaimFraudFlag::getRuleCode)
                            .toList();

            return new ClaimResponse(
                    existingClaim.getClaimRef(),
                    existingClaim.getCorrelationId(),
                    existingClaim.getStatus(),
                    existingClaim.getStatusReason(),
                    existingClaim.getFraudScore(),
                    existingClaim.getTotalAssessed(),
                    existingClaim.getTotalDepreciation(),
                    existingClaim.getDeductibleApplied(),
                    existingClaim.getCopayAmount(),
                    existingClaim.getPayableAmount(),
                    existingFlags);
        }

        // Changed payload is not allowed for approved claims
        if (existingClaim != null
                && "APPROVED".equalsIgnoreCase(existingClaim.getStatus())) {
            throw new IllegalStateException("CLAIM_LOCKED");
        }

        // R3 - Calculate line items
        List<ClaimLine> claimLines = new ArrayList<>();

        for (LineItemRequest item : request.getLineItems()) {

            LineCalculation calculation = calculateLineItem(item);

            ClaimLine line = new ClaimLine();

            line.setLineNo(item.getLineNo());
            line.setPartCode(item.getPartCode());
            line.setCategory(item.getCategory());
            line.setDescription(item.getDescription());
            line.setQuantity(item.getQuantity());
            line.setUnitPrice(item.getUnitPrice());

            line.setTaxAmount(calculation.taxAmount());
            line.setLineTotal(calculation.lineTotal());

            line.setDepPercent(BigDecimal.ZERO);
            line.setDepAmount(BigDecimal.ZERO);
            line.setApprovedAmount(BigDecimal.ZERO);
            line.setLineStatus("PAYABLE");

            claimLines.add(line);
        }

        // R4 - Depreciation
        int vehicleAge = request.getIncident().getDate().getYear()
                - request.getVehicle().getManufactureYear();

        List<String> addOns = request.getPolicy().getAddOns();

        boolean zeroDep = addOns != null
                && addOns.contains("ZERO_DEP")
                && vehicleAge <= 5;

        for (ClaimLine line : claimLines) {

            BigDecimal depPercent = getDepreciationPercent(
                    line.getCategory(),
                    vehicleAge,
                    zeroDep);

            DepreciationCalculation depreciation =
                    calculateDepreciation(
                            line.getLineTotal(),
                            depPercent);

            line.setDepPercent(depreciation.depPercent());
            line.setDepAmount(depreciation.depAmount());
            line.setApprovedAmount(depreciation.approvedAmount());
        }

        // R5 - Special claim rules
        boolean theftClaim = "THEFT".equalsIgnoreCase(
                request.getIncident().getType());

        if (theftClaim) {

            for (ClaimLine line : claimLines) {
                line.setLineStatus("EXCLUDED");
                line.setExclusionReason("THEFT_CLAIM");
                line.setDepPercent(BigDecimal.ZERO);
                line.setDepAmount(BigDecimal.ZERO);
                line.setApprovedAmount(BigDecimal.ZERO);
            }

        } else {

            for (ClaimLine line : claimLines) {

                boolean excluded = shouldExcludeFloodEnginePart(
                        request.getIncident().getType(),
                        line.getPartCode(),
                        addOns);

                if (excluded) {
                    line.setLineStatus("EXCLUDED");
                    line.setExclusionReason("FLOOD_ENGINE_EXCLUSION");
                    line.setDepPercent(BigDecimal.ZERO);
                    line.setDepAmount(BigDecimal.ZERO);
                    line.setApprovedAmount(BigDecimal.ZERO);
                }
            }
        }

        // R6 - Settlement
        boolean networkGarage = Boolean.TRUE.equals(
                request.getGarage().getNetworkGarage());

        ClaimSettlementResult settlement;

        if (theftClaim) {

            BigDecimal payable = policy.getSumInsured()
                    .subtract(policy.getDeductible())
                    .max(BigDecimal.ZERO);

            settlement = new ClaimSettlementResult(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    policy.getDeductible().min(policy.getSumInsured()),
                    BigDecimal.ZERO,
                    payable,
                    false);

        } else {

            settlement = ClaimSettlementRule.calculate(
                    claimLines,
                    policy.getSumInsured(),
                    policy.getDeductible(),
                    networkGarage);
        }

        // R7 - Fraud detection
        FraudAssessment fraudAssessment = fraudDetectionRule.assess(
                policy.getStartDate(),
                request.getIncident().getDate(),
                request.getIncident().getType(),
                request.getIncident().getFirNumber(),
                request.getPriorClaimsInLast12Months(),
                request.getSubmittedAt().toLocalDate(),
                request.getIncident().getTime(),
                networkGarage);

        int fraudScore = fraudAssessment.fraudScore();

        List<String> fraudFlags = fraudAssessment.triggeredRules();

        // R8 - Final claim decision
        ClaimDecision decision = ClaimDecisionRule.decide(
                coverageReason,
                fraudScore,
                theftClaim,
                settlement.constructiveTotalLoss(),
                settlement.payableAmount());

        // R9 - Update NCB when eligible
        PolicyNcbRule.apply(
                policy,
                decision.status(),
                settlement.payableAmount(),
                request.getIncident().getDate());

        policyRepository.save(policy);

        // Create or update claim header
        boolean isResubmission = existingClaim != null;

        ClaimHeader claim = isResubmission
                ? existingClaim
                : new ClaimHeader();

        // For resubmission, replace old child records.
        // Existing audit records are intentionally preserved.

// For resubmission, replace old child records.
// Existing audit records are intentionally preserved.
if (isResubmission) {
    claimLineRepository.deleteByClaimHeaderClaimId(
            claim.getClaimId());

    claimFraudFlagRepository.deleteByClaimHeaderClaimId(
            claim.getClaimId());

    // Force Hibernate to execute deletions before inserting replacements.
    claimLineRepository.flush();
    claimFraudFlagRepository.flush();
}


        LocalDateTime now = LocalDateTime.now();

        claim.setClaimRef(request.getClaimRef());
        claim.setCorrelationId(correlationId);
        claim.setSourceSystem(sourceSystem);
        claim.setPolicy(policy);

        claim.setAddOns(addOns == null
                ? null
                : String.join(",", addOns));

        claim.setSubmittedAtUtc(normalized.submittedAtUtc());
        claim.setVehicleRegNo(normalized.vehicleRegNo());

        claim.setVehicleDesc(
                request.getVehicle().getMake() + " "
                        + request.getVehicle().getModel());

        claim.setVehicleAgeYears(normalized.vehicleAgeYears());

        claim.setClaimantName(normalized.claimantName());
        claim.setClaimantMobileMasked(normalized.maskedMobile());
        claim.setClaimantEmail(normalized.email());

        claim.setIncidentDate(request.getIncident().getDate());
        claim.setIncidentTime(request.getIncident().getTime());
        claim.setIncidentType(request.getIncident().getType());

        claim.setIncidentLocation(
                request.getIncident().getLocation().getCity()
                        + ", "
                        + request.getIncident().getLocation().getState()
                        + " "
                        + request.getIncident().getLocation().getPincode());

        claim.setFirNumber(request.getIncident().getFirNumber());
        claim.setGarageCode(request.getGarage().getGarageCode());

        claim.setNetworkGarage(networkGarage ? "Y" : "N");

        claim.setTotalAssessed(settlement.totalAssessed());
        claim.setTotalDepreciation(settlement.totalDepreciation());
        claim.setDeductibleApplied(settlement.deductibleApplied());
        claim.setCopayAmount(settlement.copayAmount());
        claim.setPayableAmount(settlement.payableAmount());

        claim.setFraudScore(fraudScore);
        claim.setStatus(decision.status());
        claim.setStatusReason(decision.statusReason());
        claim.setPayloadHash(payloadHash);

        if (!isResubmission) {
            claim.setCreatedAt(now);
        }

        claim.setUpdatedAt(now);

        // Save claim header first to generate claim ID
        ClaimHeader savedClaim = claimHeaderRepository.save(claim);

        // Save claim line items
        for (ClaimLine line : claimLines) {
            line.setClaimHeader(savedClaim);
            claimLineRepository.save(line);
        }

        // Save fraud flags
        for (String ruleCode : fraudFlags) {

            ClaimFraudFlag flag = new ClaimFraudFlag();

            flag.setClaimHeader(savedClaim);
            flag.setRuleCode(ruleCode);
            flag.setPoints(getFraudPoints(ruleCode));

            claimFraudFlagRepository.save(flag);
        }

        // Save audit record
        ClaimAudit audit = new ClaimAudit();

        audit.setClaimHeader(savedClaim);
        audit.setEventType(isResubmission ? "RESUBMITTED" : "SUBMITTED");
        audit.setPayloadHash(payloadHash);
        audit.setCreatedAt(now);

        claimAuditRepository.save(audit);

        // Return response
        return new ClaimResponse(
                savedClaim.getClaimRef(),
                savedClaim.getCorrelationId(),
                savedClaim.getStatus(),
                savedClaim.getStatusReason(),
                savedClaim.getFraudScore(),
                savedClaim.getTotalAssessed(),
                savedClaim.getTotalDepreciation(),
                savedClaim.getDeductibleApplied(),
                savedClaim.getCopayAmount(),
                savedClaim.getPayableAmount(),
                fraudFlags);

    }
    // GET - Retrieve claim by reference
@Transactional(readOnly = true)
public ClaimResponse getClaim(String claimRef) {

    ClaimHeader claim = claimHeaderRepository
            .findByClaimRef(claimRef)
            .orElseThrow(() ->
                    new IllegalArgumentException("CLAIM_NOT_FOUND"));

    List<String> flags = claimFraudFlagRepository
            .findByClaimHeaderClaimId(claim.getClaimId())
            .stream()
            .map(ClaimFraudFlag::getRuleCode)
            .toList();

    return new ClaimResponse(
            claim.getClaimRef(),
            claim.getCorrelationId(),
            claim.getStatus(),
            claim.getStatusReason(),
            claim.getFraudScore(),
            claim.getTotalAssessed(),
            claim.getTotalDepreciation(),
            claim.getDeductibleApplied(),
            claim.getCopayAmount(),
            claim.getPayableAmount(),
            flags
    );

}




}
