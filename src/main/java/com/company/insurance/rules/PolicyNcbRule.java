
package com.company.insurance.rules;

import com.company.insurance.entity.Policy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PolicyNcbRule {

    public static void apply(
            Policy policy,
            String claimStatus,
            BigDecimal payableAmount,
            java.time.LocalDate incidentDate) {

        boolean eligibleStatus =
                "APPROVED".equalsIgnoreCase(claimStatus)
                || "REFERRED".equalsIgnoreCase(claimStatus);

        boolean hasPayableAmount =
                payableAmount.compareTo(BigDecimal.ZERO) > 0;

        if (eligibleStatus && hasPayableAmount) {
            policy.setNcbPercent(0);
            policy.setLastClaimDate(incidentDate);
            policy.setUpdatedAt(LocalDateTime.now());
        }
    }
}
