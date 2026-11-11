
package com.company.insurance.rules;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LineCalculationRuleTest {

    @Test
    void shouldCalculateLineAmounts() {

        LineCalculation result = LineCalculationRule.calculate(
                2,
                new BigDecimal("4200.00"),
                18
        );

        assertEquals(
                new BigDecimal("8400.00"),
                result.grossAmount()
        );

        assertEquals(
                new BigDecimal("1512.00"),
                result.taxAmount()
        );

        assertEquals(
                new BigDecimal("9912.00"),
                result.lineTotal()
        );
    }

    @Test
    void shouldCalculateZeroTax() {

        LineCalculation result = LineCalculationRule.calculate(
                1,
                new BigDecimal("1000.00"),
                0
        );

        assertEquals(
                new BigDecimal("0.00"),
                result.taxAmount()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                result.lineTotal()
        );
    }

    @Test
    void shouldRoundAmountsToTwoDecimals() {

        LineCalculation result = LineCalculationRule.calculate(
                1,
                new BigDecimal("100.00"),
                5
        );

        assertEquals(
                new BigDecimal("5.00"),
                result.taxAmount()
        );
    }
}
