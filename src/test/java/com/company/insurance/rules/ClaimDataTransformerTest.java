
package com.company.insurance.rules;

import com.company.insurance.dto.request.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ClaimDataTransformerTest {

    @Test
    void shouldTransformClaimData() {

        ContactRequest contact = new ContactRequest();
        contact.setMobile("+91-98765 43210");
        contact.setEmail(" Test@Mail.com ");

        ClaimantRequest claimant = new ClaimantRequest();
        claimant.setName(" surya   kumar ");
        claimant.setContact(contact);

        VehicleRequest vehicle = new VehicleRequest();
        vehicle.setRegNo("tn 09 ab 1234");
        vehicle.setManufactureYear(2023);

        IncidentRequest incident = new IncidentRequest();
        incident.setDate(LocalDate.of(2026, 10, 14));

        ClaimRequest request = new ClaimRequest();
        request.setClaimant(claimant);
        request.setVehicle(vehicle);
        request.setIncident(incident);
        request.setSubmittedAt(
                OffsetDateTime.parse("2026-10-14T09:30:00+05:30")
        );

        NormalizedClaimData result =
                ClaimDataTransformer.transform(request);

        assertEquals("Surya Kumar", result.claimantName());
        assertEquals("TN09AB1234", result.vehicleRegNo());
        assertEquals("XXXXXX3210", result.maskedMobile());
        assertEquals("test@mail.com", result.email());
        assertEquals(3, result.vehicleAgeYears());
        assertEquals(
                "2026-10-14T04:00",
                result.submittedAtUtc().toString()
        );
    }
}
