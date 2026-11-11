
package com.company.insurance.rules;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class FraudDetectionRuleTest {

    private final LocalDate policyStart =
            LocalDate.of(2026, 1, 1);

    private final LocalDate incident =
            LocalDate.of(2026, 3, 15);

    private final LocalDate submitted =
            LocalDate.of(2026, 3, 20);

    @Test
    void shouldDetectEarlyClaim() {
        FraudAssessment result = FraudDetectionRule.assess(
                policyStart,
                LocalDate.of(2026, 1, 15),
                "COLLISION",
                "FIR123",
                0,
                LocalDate.of(2026, 1, 20),
                LocalTime.NOON,
                true
        );

        assertEquals(30, result.fraudScore());
        assertTrue(result.triggeredRules().contains("F01"));
    }

    @Test
    void shouldDetectTheftWithoutFir() {
        FraudAssessment result = FraudDetectionRule.assess(
                policyStart, incident, "THEFT", null,
                0, submitted, LocalTime.NOON, true
        );

        assertEquals(40, result.fraudScore());
        assertTrue(result.triggeredRules().contains("F02"));
    }

    @Test
    void shouldDetectMultiplePreviousClaims() {
        FraudAssessment result = FraudDetectionRule.assess(
                policyStart, incident, "COLLISION", "FIR123",
                2, submitted, LocalTime.NOON, true
        );

        assertEquals(25, result.fraudScore());
        assertTrue(result.triggeredRules().contains("F03"));
    }

    @Test
    void shouldDetectLateSubmission() {
        FraudAssessment result = FraudDetectionRule.assess(
                policyStart,
                incident,
                "COLLISION",
                "FIR123",
                0,
                LocalDate.of(2026, 5, 1),
                LocalTime.NOON,
                true
        );

        assertEquals(15, result.fraudScore());
        assertTrue(result.triggeredRules().contains("F04"));
    }

    @Test
    void shouldDetectNightTimeIncident() {
        FraudAssessment result = FraudDetectionRule.assess(
                policyStart, incident, "COLLISION", "FIR123",
                0, submitted, LocalTime.of(23, 30), true
        );

        assertEquals(10, result.fraudScore());
        assertTrue(result.triggeredRules().contains("F05"));
    }

    @Test
    void shouldDetectNonNetworkGarage() {
        FraudAssessment result = FraudDetectionRule.assess(
                policyStart, incident, "COLLISION", "FIR123",
                0, submitted, LocalTime.NOON, false
        );

        assertEquals(10, result.fraudScore());
        assertTrue(result.triggeredRules().contains("F06"));
    }

    @Test
    void shouldReturnZeroWhenNoFraudRulesAreTriggered() {
        FraudAssessment result = FraudDetectionRule.assess(
                policyStart, incident, "COLLISION", "FIR123",
                0, submitted, LocalTime.NOON, true
        );

        assertEquals(0, result.fraudScore());
        assertTrue(result.triggeredRules().isEmpty());
    }
}
