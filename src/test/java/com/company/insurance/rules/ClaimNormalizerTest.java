
package com.company.insurance.rules;

import java.time.OffsetDateTime;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ClaimNormalizerTest {

    @Test
    void shouldNormalizeName() {
        assertEquals(
                "Surya Kumar",
                ClaimNormalizer.normalizeName(" surya   kumar ")
        );
    }

    
@Test
void shouldNormalizeRegistrationNumberWithHyphens() {
    assertEquals(
            "KA03MN1234",
            ClaimNormalizer.normalizeRegNo("KA-03-MN-1234")
    );
}

@Test
void shouldRejectInvalidRegistrationNumber() {
    assertThrows(
            IllegalArgumentException.class,
            () -> ClaimNormalizer.normalizeRegNo("1234TNAB")
    );
}


    @Test
    void shouldMaskMobileNumber() {
        assertEquals(
                "XXXXXX3210",
                ClaimNormalizer.maskMobile("+91-98765 43210")
        );
    }

    @Test
    void shouldNormalizeEmail() {
        assertEquals(
                "test@mail.com",
                ClaimNormalizer.normalizeEmail(" Test@Mail.com ")
        );
    }

    @Test
    void shouldConvertTimestampToUtc() {
        OffsetDateTime input =
                OffsetDateTime.parse("2026-10-14T09:30:00+05:30");

        LocalDateTime expected =
                LocalDateTime.parse("2026-10-14T04:00:00");

        assertEquals(expected, ClaimNormalizer.toUtc(input));
    }

    @Test
    void shouldCalculateVehicleAge() {
        assertEquals(
                3,
                ClaimNormalizer.calculateVehicleAge(2023, 2026)
        );
    }
}
