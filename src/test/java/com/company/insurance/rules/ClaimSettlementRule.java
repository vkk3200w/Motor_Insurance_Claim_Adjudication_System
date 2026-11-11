
package com.company.insurance.rules;

import com.company.insurance.entity.ClaimLine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class ClaimSettlementRule {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal CTL_PERCENT = BigDecimal.valueOf(75);
    private static final BigDecimal COPAY_PERCENT = BigDecimal.TEN;

    public static ClaimSettlementResult calculate(
            List<ClaimLine> lines,
            BigDecimal idv,
            BigDecimal deductible,
            boolean networkGarage
    ) {
        BigDecimal totalAssessed = ZERO;
        BigDecimal totalDepreciation = ZERO;
        BigDecimal netApproved = ZERO;

        for (ClaimLine line : lines) {

            // Include every line in total assessed
            totalAssessed = totalAssessed.add(line.getLineTotal());

            // Only payable lines contribute to approved totals
            if ("PAYABLE".equalsIgnoreCase(line.getLineStatus())) {
                totalDepreciation =
                        totalDepreciation.add(line.getDepAmount());

                netApproved =
                        netApproved.add(line.getApprovedAmount());
            }
        }

        boolean ctl = netApproved.compareTo(
                idv.multiply(CTL_PERCENT)
                        .divide(HUNDRED, 2, RoundingMode.HALF_UP)
        ) > 0;

        // CTL settlement is based on IDV
        BigDecimal settlementBase = ctl ? idv : netApproved;

        BigDecimal deductibleApplied =
                settlementBase.min(deductible);

        BigDecimal amountAfterDeductible =
                settlementBase.subtract(deductibleApplied);

        // Non-network garage has a 10% copay
        BigDecimal copayAmount = networkGarage
                ? ZERO
                : amountAfterDeductible
                    .multiply(COPAY_PERCENT)
                    .divide(HUNDRED, 2, RoundingMode.HALF_UP);

        BigDecimal payableAmount =
                amountAfterDeductible.subtract(copayAmount);

        // Final payable amount cannot exceed IDV
        payableAmount = payableAmount.min(idv).max(ZERO);

        return new ClaimSettlementResult(
                totalAssessed.setScale(2, RoundingMode.HALF_UP),
                totalDepreciation.setScale(2, RoundingMode.HALF_UP),
                netApproved.setScale(2, RoundingMode.HALF_UP),
                deductibleApplied.setScale(2, RoundingMode.HALF_UP),
                copayAmount.setScale(2, RoundingMode.HALF_UP),
                payableAmount.setScale(2, RoundingMode.HALF_UP),
                ctl
        );
    }
}
