
package com.company.insurance.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PolicyRequest {

    @NotBlank
    private String policyNo;

    private String productCode;

    private List<String> addOns;
}
