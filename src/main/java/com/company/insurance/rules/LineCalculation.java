
package com.company.insurance.rules;

import java.math.BigDecimal;

public record LineCalculation(
        BigDecimal grossAmount,
        BigDecimal taxAmount,
        BigDecimal lineTotal
) {
}
