
package com.company.insurance.rules;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpecialClaimRuleTest {

    @Test
    void shouldExcludeEnginePartForFloodWithoutProtection() {
        assertTrue(
                SpecialClaimRule.shouldExcludeFloodEnginePart(
                        "FLOOD", "ENG-001", false
                )
        );
    }

    @Test
    void shouldNotExcludeEnginePartWithProtection() {
        assertFalse(
                SpecialClaimRule.shouldExcludeFloodEnginePart(
                        "FLOOD", "ENG-001", true
                )
        );
    }

    @Test
    void shouldNotExcludeEnginePartForCollision() {
        assertFalse(
                SpecialClaimRule.shouldExcludeFloodEnginePart(
                        "COLLISION", "ENG-001", false
                )
        );
    }

    @Test
    void shouldIdentifyTheftClaim() {
        assertTrue(SpecialClaimRule.isTheftClaim("THEFT"));
    }

    @Test
    void shouldNotIdentifyCollisionAsTheft() {
        assertFalse(SpecialClaimRule.isTheftClaim("COLLISION"));
    }
}
