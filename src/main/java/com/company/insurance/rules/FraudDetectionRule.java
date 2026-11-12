
package com.company.insurance.rules;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class FraudDetectionRule {

    public static FraudAssessment assess(
            LocalDate policyStartDate,
            LocalDate incidentDate,
            String incidentType,
            String firNumber,
            int priorClaims,
            LocalDate submittedDate,
            LocalTime incidentTime,
            boolean networkGarage) {

        List<String> triggeredRules = new ArrayList<>();
        int fraudScore = 0;

        // F01: Incident within 7 days of policy start
        // F01: Incident within 30 days of policy start
long days = ChronoUnit.DAYS.between(policyStartDate, incidentDate);

if (days >= 0 && days <= 30) {
            triggeredRules.add("F01");
            fraudScore += 30;
        }

        // F02: Theft without FIR
        if ("THEFT".equalsIgnoreCase(incidentType)
                && (firNumber == null || firNumber.isBlank())) {
            triggeredRules.add("F02");
            fraudScore += 40;
        }

        // F03: Two or more previous claims
        if (priorClaims >= 2) {
            triggeredRules.add("F03");
            fraudScore += 25;
        }

        // F04: Submission more than 30 days after incident
        long delay = ChronoUnit.DAYS.between(incidentDate, submittedDate);

        if (delay > 30) {
            triggeredRules.add("F04");
            fraudScore += 15;
        }

        // F05: Incident between 23:00 and 04:59
        if (incidentTime.isBefore(LocalTime.of(5, 0))
                || !incidentTime.isBefore(LocalTime.of(23, 0))) {
            triggeredRules.add("F05");
            fraudScore += 10;
        }

        // F06: Non-network garage
        if (!networkGarage) {
            triggeredRules.add("F06");
            fraudScore += 10;
        }

        return new FraudAssessment(fraudScore, triggeredRules);
    }
}
