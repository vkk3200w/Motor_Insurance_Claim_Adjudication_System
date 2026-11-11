
package com.company.insurance.rules;

import com.company.insurance.entity.Policy;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PolicyCoverageRuleTest {

    private Policy createPolicy(String product, String status) {
        Policy policy = new Policy();

        policy.setProductCode(product);
        policy.setStatus(status);
        policy.setStartDate(LocalDate.of(2026, 1, 1));
        policy.setEndDate(LocalDate.of(2026, 12, 31));

        return policy;
    }

    @Test
    void shouldRejectThirdPartyPolicy() {
        Policy policy = createPolicy("TP", "ACTIVE");

        assertEquals(
                "TP_NO_OWN_DAMAGE",
                PolicyCoverageRule.checkCoverage(
                        policy, LocalDate.of(2026, 6, 1))
        );
    }

    @Test
    void shouldRejectIncidentOutsidePolicyPeriod() {
        Policy policy = createPolicy("COMP", "ACTIVE");

        assertEquals(
                "OUTSIDE_POLICY_PERIOD",
                PolicyCoverageRule.checkCoverage(
                        policy, LocalDate.of(2027, 1, 1))
        );
    }

    @Test
    void shouldRejectInactivePolicy() {
        Policy policy = createPolicy("COMP", "LAPSED");

        assertEquals(
                "POLICY_INACTIVE",
                PolicyCoverageRule.checkCoverage(
                        policy, LocalDate.of(2026, 6, 1))
        );
    }

    @Test
    void shouldApproveValidCoverage() {
        Policy policy = createPolicy("COMP", "ACTIVE");

        assertNull(
                PolicyCoverageRule.checkCoverage(
                        policy, LocalDate.of(2026, 6, 1))
        );
    }
}
