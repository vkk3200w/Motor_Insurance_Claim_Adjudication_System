
package com.company.insurance.rules;

import java.util.List;

public record FraudAssessment(
        int fraudScore,
        List<String> triggeredRules
) {
}
