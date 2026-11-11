
package com.company.insurance.rules;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ClaimDecisionRule {

    public static ClaimDecision decide(
            String coverageRejectionReason,
            int fraudScore,
            boolean theftClaim,
            boolean constructiveTotalLoss,
            BigDecimal payableAmount
    ) {
        List<String> reasons = new ArrayList<>();

        // Priority 1: Policy rejection
        if (coverageRejectionReason != null) {
            return new ClaimDecision(
                    "REJECTED",
                    coverageRejectionReason
            );
        }

        // Priority 2: Fraud score >= 50
        if (fraudScore >= 50) {
            reasons.add("HIGH_FRAUD_SCORE");
        }

        // Priority 3: Theft claim
        if (theftClaim) {
            reasons.add("THEFT_REVIEW");
        }

        // Priority 4: Constructive Total Loss
        if (constructiveTotalLoss) {
            reasons.add("CONSTRUCTIVE_TOTAL_LOSS");
        }

        // Priority 5: Payable amount > 200000
        if (payableAmount.compareTo(
                new BigDecimal("200000")) > 0) {
            reasons.add("HIGH_VALUE_CLAIM");
        }

        if (!reasons.isEmpty()) {
            return new ClaimDecision(
                    "REFERRED",
                    String.join(",", reasons)
            );
        }

        // Priority 6: Positive payable amount
        if (payableAmount.compareTo(BigDecimal.ZERO) > 0) {
            return new ClaimDecision("APPROVED", null);
        }

        // Priority 7: No payable amount
        return new ClaimDecision("ZERO_PAYABLE", null);
    }
}
