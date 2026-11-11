
package com.company.insurance.rules;

public record FraudFlagResult(
    String ruleCode,
    int points
) {}
