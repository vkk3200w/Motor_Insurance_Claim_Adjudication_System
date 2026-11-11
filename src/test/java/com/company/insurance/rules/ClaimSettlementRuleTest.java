
package com.company.insurance.rules;

import com.company.insurance.entity.ClaimLine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClaimSettlementRuleTest {

    private ClaimLine createLine(
            String total,
            String depreciation,
            String approved,
            String status
    ) {
        ClaimLine line = new ClaimLine();
        line.setLineTotal(new BigDecimal(total));
        line.setDepAmount(new BigDecimal(depreciation));
        line.setApprovedAmount(new BigDecimal(approved));
        line.setLineStatus(status);
        return line;
    }

    @Test
    void shouldCalculateNetworkGarageSettlement() {
        List<ClaimLine> lines = List.of(
                createLine("100000", "10000", "90000", "PAYABLE")
        );

        ClaimSettlementResult result =
                ClaimSettlementRule.calculate(
                        lines,
                        new BigDecimal("200000"),
                        new BigDecimal("2000"),
                        true
                );

        assertEquals(new BigDecimal("100000.00"), result.totalAssessed());
        assertEquals(new BigDecimal("10000.00"), result.totalDepreciation());
        assertEquals(new BigDecimal("90000.00"), result.netApproved());
        assertEquals(new BigDecimal("2000.00"), result.deductibleApplied());
        assertEquals(new BigDecimal("0.00"), result.copayAmount());
        assertEquals(new BigDecimal("88000.00"), result.payableAmount());
        assertFalse(result.constructiveTotalLoss());
    }

    @Test
    void shouldApplyNonNetworkCopay() {
        List<ClaimLine> lines = List.of(
                createLine("100000", "10000", "90000", "PAYABLE")
        );

        ClaimSettlementResult result =
                ClaimSettlementRule.calculate(
                        lines,
                        new BigDecimal("200000"),
                        new BigDecimal("2000"),
                        false
                );

        assertEquals(new BigDecimal("8800.00"), result.copayAmount());
        assertEquals(new BigDecimal("79200.00"), result.payableAmount());
    }

    @Test
    void shouldIdentifyConstructiveTotalLoss() {
        List<ClaimLine> lines = List.of(
                createLine("160000", "90000", "151000", "PAYABLE")
        );

        ClaimSettlementResult result =
                ClaimSettlementRule.calculate(
                        lines,
                        new BigDecimal("200000"),
                        new BigDecimal("2000"),
                        true
                );

        assertTrue(result.constructiveTotalLoss());
        assertEquals(new BigDecimal("198000.00"), result.payableAmount());
    }
}
