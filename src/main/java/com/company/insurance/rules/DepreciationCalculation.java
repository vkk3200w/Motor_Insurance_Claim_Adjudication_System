
package com.company.insurance.rules;

import java.math.BigDecimal;

public record DepreciationCalculation(
        BigDecimal depPercent,
        BigDecimal depAmount,
        BigDecimal approvedAmount
) {
}
