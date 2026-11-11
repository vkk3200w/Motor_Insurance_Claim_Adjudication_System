
package com.company.insurance.rules;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ClaimDecisionRule {

    public static ClaimDecision decide(
            String rejectionReason,
            int fraudScore,
            boolean theftClaim,
            boolean constructiveTotalLoss,
            BigDecimal payableAmount) {

        // Priority 1: Reject claim
        if (rejectionReason != null && !rejectionReason.isBlank()) {
            return new ClaimDecision("REJECTED", rejectionReason);
        }

        List<String> reasons = new ArrayList<>();

        // Priority 2: High fraud score
        if (fraudScore >= 50) {
            reasons.add("HIGH_FRAUD_SCORE");
        }

        // Priority 3: Theft claim
        if (theftClaim) {
            reasons.add("THEFT_REVIEW");
        }

        // Priority 4: Constructive total loss
        if (constructiveTotalLoss) {
            reasons.add("CONSTRUCTIVE_TOTAL_LOSS");
        }

        // Priority 5: High-value claim
        if (payableAmount.compareTo(new BigDecimal("200000")) > 0) {
            reasons.add("HIGH_VALUE_CLAIM");
        }

        // Return referral with combined reasons
        if (!reasons.isEmpty()) {
            return new ClaimDecision(
                    "REFERRED",
                    String.join(",", reasons)
            );
        }

        // Priority 6: Approve positive payable amount
        if (payableAmount.compareTo(BigDecimal.ZERO) > 0) {
            return new ClaimDecision("APPROVED", null);
        }

        // Priority 7: Zero payable
        return new ClaimDecision("ZERO_PAYABLE", null);
    }
}
