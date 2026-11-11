
package com.company.insurance.rules;

import com.company.insurance.dto.request.ClaimRequest;
import com.company.insurance.dto.request.ContactRequest;
import com.company.insurance.dto.request.ClaimantRequest;

public class ClaimDataTransformer {

    public static NormalizedClaimData transform(ClaimRequest request) {

        ClaimantRequest claimant = request.getClaimant();
        ContactRequest contact = claimant.getContact();

        String normalizedName =
                ClaimNormalizer.normalizeName(claimant.getName());

        String normalizedRegNo =
                ClaimNormalizer.normalizeRegNo(
                        request.getVehicle().getRegNo()
                );

        String maskedMobile =
                ClaimNormalizer.maskMobile(contact.getMobile());

        String normalizedEmail =
                ClaimNormalizer.normalizeEmail(contact.getEmail());

        var submittedAtUtc =
                ClaimNormalizer.toUtc(request.getSubmittedAt());

        int vehicleAge =
                ClaimNormalizer.calculateVehicleAge(
                        request.getVehicle().getManufactureYear(),
                        request.getIncident().getDate().getYear()
                );

        return new NormalizedClaimData(
                normalizedName,
                normalizedRegNo,
                maskedMobile,
                normalizedEmail,
                submittedAtUtc,
                vehicleAge
        );
    }
}
