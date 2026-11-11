
package com.company.insurance.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Pattern;

@Getter
@Setter
public class IncidentRequest {

    @NotNull
    private LocalDate date;

    private LocalTime time;

    @NotBlank
    @Pattern(regexp = "COLLISION|THEFT|FLOOD|FIRE")
    private String type;

    @NotNull
    @Valid
    private IncidentLocationRequest location;

    private Boolean policeReported;

    private String firNumber;

    private String description;
}
