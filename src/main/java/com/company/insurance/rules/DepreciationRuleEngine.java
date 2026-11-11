
package com.company.insurance.rules;

import com.company.insurance.entity.DepreciationRule;
import com.company.insurance.repository.DepreciationRuleRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DepreciationRuleEngine {

    private final DepreciationRuleRepository repository;

    public DepreciationRuleEngine(
            DepreciationRuleRepository repository
    ) {
        this.repository = repository;
    }

    public BigDecimal getDepreciationPercent(
            String category,
            int vehicleAge,
            boolean zeroDep
    ) {
        if (zeroDep && vehicleAge <= 5) {
            return BigDecimal.ZERO;
        }

        List<DepreciationRule> rules =
                repository.findByCategory(category);

        return rules.stream()
                .filter(rule ->
                        vehicleAge >= rule.getMinAge()
                        && vehicleAge <= rule.getMaxAge())
                .findFirst()
                .map(DepreciationRule::getDepPercent)
                .orElseThrow(() -> new IllegalStateException(
                        "No depreciation rule found for category: "
                                + category));
    }

    public DepreciationCalculation calculateDepreciation(
            BigDecimal lineTotal,
            BigDecimal depPercent
    ) {
        BigDecimal depAmount = lineTotal
                .multiply(depPercent)
                .divide(BigDecimal.valueOf(100), 2,
                        java.math.RoundingMode.HALF_UP);

        BigDecimal approvedAmount = lineTotal
                .subtract(depAmount)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        return new DepreciationCalculation(
                depPercent, depAmount, approvedAmount);
    }
}
