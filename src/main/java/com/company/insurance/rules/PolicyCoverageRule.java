
package com.company.insurance.rules;

import com.company.insurance.entity.Policy;
import java.time.LocalDate;

public class PolicyCoverageRule {

    public static String checkCoverage(
            Policy policy,
            LocalDate incidentDate
    ) {
        if ("TP".equalsIgnoreCase(policy.getProductCode())) {
            return "TP_NO_OWN_DAMAGE";
        }

        if (incidentDate.isBefore(policy.getStartDate())
                || incidentDate.isAfter(policy.getEndDate())) {
            return "OUTSIDE_POLICY_PERIOD";
        }

        if (!"ACTIVE".equalsIgnoreCase(policy.getStatus())) {
            return "POLICY_INACTIVE";
        }

        return null;
    }
}
