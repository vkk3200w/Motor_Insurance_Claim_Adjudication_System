
package com.company.insurance.rules;

import com.company.insurance.entity.Policy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PolicyNcbRule {

    public static void apply(
            Policy policy,
            String claimStatus,
            BigDecimal payableAmount,
            LocalDate incidentDate
    ) {
        boolean eligibleStatus =
                "APPROVED".equalsIgnoreCase(claimStatus)
                || "REFERRED".equalsIgnoreCase(claimStatus);

        if (eligibleStatus && payableAmount.compareTo(BigDecimal.ZERO) > 0) {
            policy.setNcbPercent(0);
            policy.setLastClaimDate(incidentDate);
            policy.setUpdatedAt(LocalDateTime.now());
        }
    }
}
