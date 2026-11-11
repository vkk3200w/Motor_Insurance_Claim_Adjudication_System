
package com.company.insurance.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClaimantRequest {

    @NotBlank
    private String name;

    private String relationship;

    @NotNull
    @Valid
    private ContactRequest contact;
}
