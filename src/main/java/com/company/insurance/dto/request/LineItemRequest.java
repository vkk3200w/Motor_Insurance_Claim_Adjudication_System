
package com.company.insurance.dto.request;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

import java.math.BigDecimal;

@Getter
@Setter
public class LineItemRequest {

    @NotNull
    @Min(1)
    private Integer lineNo;

    @NotBlank
    private String partCode;

    @NotBlank
    @Pattern(regexp = "METAL|PLASTIC|RUBBER|GLASS|PAINT|LABOUR")
    private String category;

    private String description;

    @NotNull
    @Min(1)
    @Max(99)
    private Integer quantity;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal unitPrice;

    @NotNull
    private Integer taxPercent;

    @AssertTrue(message = "Tax percent must be 0, 5, 12, 18 or 28")
    public boolean isTaxPercentValid() {
        return taxPercent == null ||
            List.of(0, 5, 12, 18, 28).contains(taxPercent);
}
}
