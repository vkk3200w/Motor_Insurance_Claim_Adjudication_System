
package com.company.insurance.rules;

import java.math.BigDecimal;

public record SettlementCalculation(
        BigDecimal totalAssessed,
        BigDecimal totalDepreciation,
        BigDecimal netApproved,
        BigDecimal deductibleApplied,
        BigDecimal copayAmount,
        BigDecimal payableAmount,
        boolean constructiveTotalLoss
) {
}
