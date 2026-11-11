
package com.company.insurance.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ClaimResponse(
        String claimRef,
        String correlationId,
        String status,
        String statusReason,
        int fraudScore,
        BigDecimal totalAssessed,
        BigDecimal totalDepreciation,
        BigDecimal deductibleApplied,
        BigDecimal copayAmount,
        BigDecimal payableAmount,
        List<String> fraudFlags
) {
}
