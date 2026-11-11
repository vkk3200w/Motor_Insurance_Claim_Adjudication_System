
package com.company.insurance.rules;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ClaimDecisionRuleTest {

    @Test
    void shouldRejectClaimWhenCoverageFails() {
        ClaimDecision result = ClaimDecisionRule.decide(
                "POLICY_INACTIVE",
                80,
                false,
                false,
                new BigDecimal("50000")
        );

        assertEquals("REJECTED", result.status());
        assertEquals("POLICY_INACTIVE", result.statusReason());
    }

    @Test
    void shouldReferClaimWhenFraudScoreIsHigh() {
        ClaimDecision result = ClaimDecisionRule.decide(
                null, 50, false, false,
                new BigDecimal("50000")
        );

        assertEquals("REFERRED", result.status());
        assertEquals("HIGH_FRAUD_SCORE", result.statusReason());
    }

    @Test
    void shouldReferTheftClaim() {
        ClaimDecision result = ClaimDecisionRule.decide(
                null, 0, true, false,
                new BigDecimal("50000")
        );

        assertEquals("REFERRED", result.status());
        assertEquals("THEFT_REVIEW", result.statusReason());
    }

    @Test
    void shouldReferConstructiveTotalLoss() {
        ClaimDecision result = ClaimDecisionRule.decide(
                null, 0, false, true,
                new BigDecimal("50000")
        );

        assertEquals("REFERRED", result.status());
        assertEquals("CONSTRUCTIVE_TOTAL_LOSS", result.statusReason());
    }

    @Test
    void shouldReferHighValueClaim() {
        ClaimDecision result = ClaimDecisionRule.decide(
                null, 0, false, false,
                new BigDecimal("200001")
        );

        assertEquals("REFERRED", result.status());
        assertEquals("HIGH_VALUE_CLAIM", result.statusReason());
    }

    @Test
    void shouldApproveNormalClaim() {
        ClaimDecision result = ClaimDecisionRule.decide(
                null, 0, false, false,
                new BigDecimal("50000")
        );

        assertEquals("APPROVED", result.status());
        assertNull(result.statusReason());
    }

    @Test
    void shouldReturnZeroPayable() {
        ClaimDecision result = ClaimDecisionRule.decide(
                null, 0, false, false,
                BigDecimal.ZERO
        );

        assertEquals("ZERO_PAYABLE", result.status());
        assertNull(result.statusReason());
    }

    @Test
    void shouldCombineMultipleReferralReasons() {
        ClaimDecision result = ClaimDecisionRule.decide(
                null, 60, true, false,
                new BigDecimal("50000")
        );

        assertEquals("REFERRED", result.status());
        assertEquals(
                "HIGH_FRAUD_SCORE,THEFT_REVIEW",
                result.statusReason()
        );
    }
}
