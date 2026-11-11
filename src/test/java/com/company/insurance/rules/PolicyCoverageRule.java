
package com.company.insurance.rules;

import com.company.insurance.entity.Policy;

import java.time.LocalDate;

public class PolicyCoverageRule {

    public static String checkCoverage(Policy policy,
                                       LocalDate incidentDate) {

        // R2.1: Third-party policies do not cover own damage
        if ("TP".equalsIgnoreCase(policy.getProductCode())) {
            return "TP_NO_OWN_DAMAGE";
        }

        // R2.2: Check whether incident is within policy period
        if (incidentDate.isBefore(policy.getStartDate())
                || incidentDate.isAfter(policy.getEndDate())) {
            return "OUTSIDE_POLICY_PERIOD";
        }

        // R2.3: Check policy status
        if (!"ACTIVE".equalsIgnoreCase(policy.getStatus())) {
            return "POLICY_INACTIVE";
        }

        return null;
    }
}
