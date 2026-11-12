
package com.company.insurance.rules;

import com.company.insurance.entity.ClaimLine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class ClaimSettlementRule {

    public static ClaimSettlementResult calculate(
            List<ClaimLine> lines,
            BigDecimal idv,
            BigDecimal deductible,
            boolean networkGarage) {

        BigDecimal totalAssessed = BigDecimal.ZERO;
        BigDecimal totalDepreciation = BigDecimal.ZERO;
        BigDecimal netApproved = BigDecimal.ZERO;

        for (ClaimLine line : lines) {

            totalAssessed = totalAssessed.add(line.getLineTotal());

            if ("PAYABLE".equalsIgnoreCase(line.getLineStatus())) {
                totalDepreciation =
                        totalDepreciation.add(line.getDepAmount());

                netApproved =
                        netApproved.add(line.getApprovedAmount());
            }
        }

        totalAssessed = totalAssessed.setScale(2, RoundingMode.HALF_UP);
        totalDepreciation =
                totalDepreciation.setScale(2, RoundingMode.HALF_UP);
        netApproved = netApproved.setScale(2, RoundingMode.HALF_UP);

        boolean ctl = netApproved.compareTo(
                idv.multiply(new BigDecimal("0.75"))) > 0;

        BigDecimal settlementBase = ctl ? idv : netApproved;

        BigDecimal deductibleApplied =
                settlementBase.min(deductible);

        BigDecimal afterDeductible =
                settlementBase.subtract(deductibleApplied);

        BigDecimal copayAmount = networkGarage
        ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        : afterDeductible.multiply(new BigDecimal("0.10"))
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal payableAmount = afterDeductible
                .subtract(copayAmount)
                .max(BigDecimal.ZERO)
                .min(idv)
                .setScale(2, RoundingMode.HALF_UP);

        return new ClaimSettlementResult(
                totalAssessed,
                totalDepreciation,
                netApproved,
                deductibleApplied.setScale(2, RoundingMode.HALF_UP),
                copayAmount,
                payableAmount,
                ctl
        );
    }
}
