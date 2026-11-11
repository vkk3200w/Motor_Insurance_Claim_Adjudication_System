
package com.company.insurance.rules;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LineCalculationRule {

    public static LineCalculation calculate(
            Integer quantity,
            BigDecimal unitPrice,
            Integer taxPercent) {

        // R3.1: Calculate gross amount
        BigDecimal grossAmount = unitPrice
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);

        // R3.2: Calculate tax amount
        BigDecimal taxAmount = grossAmount
                .multiply(BigDecimal.valueOf(taxPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // R3.3: Calculate total amount
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
