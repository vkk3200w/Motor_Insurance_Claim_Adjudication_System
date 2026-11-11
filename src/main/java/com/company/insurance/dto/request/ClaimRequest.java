
package com.company.insurance.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.validation.constraints.AssertTrue;

@Getter
@Setter
public class ClaimRequest {

    @NotBlank
    @Pattern(regexp = "CLM-\\d{4}-\\d{6}")
    private String claimRef;

    @NotNull
    private OffsetDateTime submittedAt;

    @NotNull
    @Valid
    private PolicyRequest policy;

    @NotNull
    @Valid
    private VehicleRequest vehicle;

    @NotNull
    @Valid
    private ClaimantRequest claimant;

    @NotNull
    @Valid
    private IncidentRequest incident;

    @NotNull
    @Valid
    private GarageRequest garage;

    @NotNull
    private Integer priorClaimsInLast12Months;

    @NotNull
    @Size(min = 1, max = 50)
    @Valid
    private List<LineItemRequest> lineItems;

    @AssertTrue(message = "Line numbers must be unique")
    public boolean isLineNumbersUnique() {

    if (lineItems == null) {
        return true;
    }

    long total = lineItems.stream()
            .filter(item -> item != null && item.getLineNo() != null)
            .count();

    long unique = lineItems.stream()
            .filter(item -> item != null && item.getLineNo() != null)
            .map(LineItemRequest::getLineNo)
            .distinct()
            .count();

    return total == unique;
}

@AssertTrue(message = "Vehicle manufacture year cannot be after incident year")
public boolean isManufactureYearValid() {

    if (vehicle == null || vehicle.getManufactureYear() == null
            || incident == null || incident.getDate() == null) {
        return true;
    }

    return vehicle.getManufactureYear() <= incident.getDate().getYear();
}

}
