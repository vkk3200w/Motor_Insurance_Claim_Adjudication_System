
package com.company.insurance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GarageRequest {

    @NotBlank
    private String garageCode;

    @NotNull
    private Boolean networkGarage;
}
