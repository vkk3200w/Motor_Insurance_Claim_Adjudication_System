
package com.company.insurance.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.AssertTrue;

@Getter
@Setter
public class ContactRequest {

    @NotBlank
    private String mobile;

    @Email
    private String email;
    @AssertTrue(message = "Mobile number must contain exactly 10 digits")
    public boolean isMobileValid() {

    if (mobile == null || mobile.isBlank()) {
        return true;
    }

    String cleaned = mobile.replace("+91", "")
                           .replaceAll("[\\s-]", "");

    return cleaned.matches("\\d{10}");
}
}
