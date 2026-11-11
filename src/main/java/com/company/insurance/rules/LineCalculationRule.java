
package com.company.insurance.rules;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LineCalculationRule {

    public static LineCalculation calculate(
            Integer quantity,
            BigDecimal unitPrice,
            Integer taxPercent
    ) {
        BigDecimal grossAmount = unitPrice
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxAmount = grossAmount
                .multiply(BigDecimal.valueOf(taxPercent))
                .divide(BigDecimal.valueOf(100), 2,
                        RoundingMode.HALF_UP);

        BigDecimal lineTotal = grossAmount
                .add(taxAmount)
                .setScale(2, RoundingMode.HALF_UP);

        return new LineCalculation(
                grossAmount,
                taxAmount,
                lineTotal
        );
    }
}
