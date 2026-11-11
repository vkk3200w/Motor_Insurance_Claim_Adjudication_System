
package com.company.insurance.rules;

import com.company.insurance.entity.Policy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PolicyNcbRuleTest {

    private Policy createPolicy() {
        Policy policy = new Policy();
        policy.setNcbPercent(25);
        return policy;
    }

    @Test
    void shouldResetNcbForApprovedClaim() {
        Policy policy = createPolicy();

        PolicyNcbRule.apply(
                policy,
                "APPROVED",
                new BigDecimal("50000"),
                LocalDate.of(2026, 10, 10)
        );

        assertEquals(0, policy.getNcbPercent());
        assertEquals(
                LocalDate.of(2026, 10, 10),
                policy.getLastClaimDate()
        );
        assertNotNull(policy.getUpdatedAt());
    }

    @Test
    void shouldResetNcbForReferredClaim() {
        Policy policy = createPolicy();

        PolicyNcbRule.apply(
                policy,
                "REFERRED",
                new BigDecimal("30000"),
                LocalDate.of(2026, 10, 10)
        );

        assertEquals(0, policy.getNcbPercent());
    }

    @Test
    void shouldNotResetNcbForRejectedClaim() {
        Policy policy = createPolicy();

        PolicyNcbRule.apply(
                policy,
                "REJECTED",
                new BigDecimal("50000"),
                LocalDate.of(2026, 10, 10)
        );

        assertEquals(25, policy.getNcbPercent());
        assertNull(policy.getLastClaimDate());
        assertNull(policy.getUpdatedAt());
    }

    @Test
    void shouldNotResetNcbForZeroPayableClaim() {
        Policy policy = createPolicy();

        PolicyNcbRule.apply(
                policy,
                "ZERO_PAYABLE",
                BigDecimal.ZERO,
                LocalDate.of(2026, 10, 10)
        );

        assertEquals(25, policy.getNcbPercent());
        assertNull(policy.getLastClaimDate());
    }
}
