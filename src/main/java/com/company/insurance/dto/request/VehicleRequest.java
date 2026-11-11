
package com.company.insurance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleRequest {

    @NotBlank
    private String regNo;

    private String make;

    private String model;

    @NotNull
    @Min(1900)
    private Integer manufactureYear;

    private String fuelType;
}
