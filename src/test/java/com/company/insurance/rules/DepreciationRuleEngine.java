
package com.company.insurance.rules;

import com.company.insurance.entity.DepreciationRule;
import com.company.insurance.repository.DepreciationRuleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DepreciationRuleEngine {

    private final DepreciationRuleRepository repository;

    public DepreciationRuleEngine(DepreciationRuleRepository repository) {
        this.repository = repository;
    }

    public BigDecimal getDepreciationPercent(
            String category,
            int vehicleAge,
            boolean zeroDep
    ) {

        // R4.1: ZERO_DEP applies only when vehicle age is 5 or less
        if (zeroDep && vehicleAge <= 5) {
            return BigDecimal.ZERO;
        }

        // Retrieve rules from the database
        List<DepreciationRule> rules =
                repository.findByCategory(category);

        // Find the matching vehicle-age range
        return rules.stream()
                .filter(rule -> vehicleAge >= rule.getMinAge()
                        && vehicleAge <= rule.getMaxAge())
                .findFirst()
                .map(DepreciationRule::getDepPercent)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No depreciation rule found for category: "
                                        + category
                        )
                );
    }
    public DepreciationCalculation calculateDepreciation(
        BigDecimal lineTotal,
        BigDecimal depPercent
) {

    // Calculate depreciation amount
    BigDecimal depAmount = lineTotal
            .multiply(depPercent)
            .divide(BigDecimal.valueOf(100), 2,
                    java.math.RoundingMode.HALF_UP);

    // Calculate approved amount
    BigDecimal approvedAmount = lineTotal
            .subtract(depAmount)
            .setScale(2, java.math.RoundingMode.HALF_UP);

    return new DepreciationCalculation(
            depPercent,
            depAmount,
            approvedAmount
    );
}
}