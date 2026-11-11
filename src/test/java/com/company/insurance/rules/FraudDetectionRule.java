
package com.company.insurance.rules;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class FraudDetectionRule {

    public static FraudAssessment assess(
            LocalDate policyStartDate,
            LocalDate incidentDate,
            String incidentType,
            String firNumber,
            int priorClaims,
            LocalDate submittedDate,
            LocalTime incidentTime,
            boolean networkGarage
    ) {
        int score = 0;
        List<String> triggeredRules = new ArrayList<>();

        // F01: Incident within 30 days of policy start
        long daysFromStart =
                ChronoUnit.DAYS.between(policyStartDate, incidentDate);

        if (daysFromStart >= 0 && daysFromStart <= 30) {
            score += 30;
            triggeredRules.add("F01");
        }

        // F02: Theft claim with missing FIR
        if ("THEFT".equalsIgnoreCase(incidentType)
                && (firNumber == null || firNumber.isBlank())) {
            score += 40;
            triggeredRules.add("F02");
        }

        // F03: Two or more previous claims
        if (priorClaims >= 2) {
            score += 25;
            triggeredRules.add("F03");
        }

        // F04: Claim submitted more than 30 days after incident
        long daysToSubmit =
                ChronoUnit.DAYS.between(incidentDate, submittedDate);

        if (daysToSubmit > 30) {
            score += 15;
            triggeredRules.add("F04");
        }

        // F05: Incident between 23:00 and 04:59
        if (incidentTime != null
                && (incidentTime.isAfter(LocalTime.of(22, 59))
                || incidentTime.isBefore(LocalTime.of(5, 0)))) {
            score += 10;
            triggeredRules.add("F05");
        }

        // F06: Non-network garage
        if (!networkGarage) {
            score += 10;
            triggeredRules.add("F06");
        }

        return new FraudAssessment(score, triggeredRules);
    }
}
