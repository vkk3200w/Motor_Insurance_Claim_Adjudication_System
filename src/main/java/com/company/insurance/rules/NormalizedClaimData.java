
package com.company.insurance.rules;

import java.time.LocalDateTime;

public record NormalizedClaimData(
        String claimantName,
        String vehicleRegNo,
        String maskedMobile,
        String email,
        LocalDateTime submittedAtUtc,
        int vehicleAgeYears
) {
}
