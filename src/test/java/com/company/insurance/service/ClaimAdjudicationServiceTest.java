
package com.company.insurance.service;

import com.company.insurance.entity.ClaimHeader;
import com.company.insurance.entity.Policy;
import com.company.insurance.exception.PolicyNotFoundException;
import com.company.insurance.repository.*;
import com.company.insurance.rules.*;

import com.company.insurance.dto.request.LineItemRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimAdjudicationServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private DepreciationRuleRepository depreciationRuleRepository;

    @Mock
    private ClaimHeaderRepository claimHeaderRepository;

    @Mock
    private ClaimLineRepository claimLineRepository;

    @Mock
    private ClaimFraudFlagRepository claimFraudFlagRepository;

    @Mock
    private ClaimAuditRepository claimAuditRepository;

    @Mock
    private DepreciationRuleEngine depreciationRuleEngine;

    @Mock
    private FraudDetectionRule fraudDetectionRule;

    @InjectMocks
    private ClaimAdjudicationService service;

    private String hash(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(
                digest.digest(input.getBytes(StandardCharsets.UTF_8))
        );
    }

    @Test
    void identicalReplayReturnsTrue() throws Exception {
        String body = "{\"claimRef\":\"CLM-2026-000001\"}";

        ClaimHeader claim = new ClaimHeader();
        claim.setPayloadHash(hash(body));

        when(claimHeaderRepository.findByClaimRef("CLM-2026-000001"))
                .thenReturn(Optional.of(claim));

        assertTrue(service.isIdenticalReplay("CLM-2026-000001", body));
    }

    @Test
    void differentPayloadReturnsFalse() throws Exception {
        ClaimHeader claim = new ClaimHeader();
        claim.setPayloadHash(hash("original"));

        when(claimHeaderRepository.findByClaimRef("CLM-2026-000001"))
                .thenReturn(Optional.of(claim));

        assertFalse(service.isIdenticalReplay(
                "CLM-2026-000001", "modified"));
    }

    @Test
    void missingClaimReturnsFalseForReplay() {
        when(claimHeaderRepository.findByClaimRef("CLM-2026-000001"))
                .thenReturn(Optional.empty());

        assertFalse(service.isIdenticalReplay(
                "CLM-2026-000001", "request"));
    }

    @Test
    void findPolicyReturnsExistingPolicy() {
        Policy policy = new Policy();
        policy.setPolicyNo("MTR-TN-778812");

        when(policyRepository.findById("MTR-TN-778812"))
                .thenReturn(Optional.of(policy));

        Policy result = service.findPolicyByNumber("MTR-TN-778812");

        assertEquals("MTR-TN-778812", result.getPolicyNo());
    }

    @Test
    void findPolicyThrowsWhenPolicyDoesNotExist() {
        when(policyRepository.findById("INVALID"))
                .thenReturn(Optional.empty());

        assertThrows(
                PolicyNotFoundException.class,
                () -> service.findPolicyByNumber("INVALID")
        );
    }

    @Test
    void calculateLineItemCalculatesTaxAndTotal() {
        LineItemRequest item = new LineItemRequest();
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("100.00"));
        item.setTaxPercent(18);

        LineCalculation result = service.calculateLineItem(item);

        assertEquals(new BigDecimal("36.00"), result.taxAmount());
        assertEquals(new BigDecimal("236.00"), result.lineTotal());
    }

    @Test
    void depreciationPercentDelegatesToEngine() {
        when(depreciationRuleEngine.getDepreciationPercent(
                "METAL", 3, false))
                .thenReturn(new BigDecimal("15.00"));

        BigDecimal result = service.getDepreciationPercent(
                "METAL", 3, false);

        assertEquals(new BigDecimal("15.00"), result);

        verify(depreciationRuleEngine)
                .getDepreciationPercent("METAL", 3, false);
    }

    @Test
    void calculateDepreciationDelegatesToEngine() {
        DepreciationCalculation expected =
                new DepreciationCalculation(
                        new BigDecimal("10.00"),
                        new BigDecimal("100.00"),
                        new BigDecimal("900.00"));

        when(depreciationRuleEngine.calculateDepreciation(
                new BigDecimal("1000.00"),
                new BigDecimal("10.00")))
                .thenReturn(expected);

        DepreciationCalculation result =
                service.calculateDepreciation(
                        new BigDecimal("1000.00"),
                        new BigDecimal("10.00"));

        assertEquals(new BigDecimal("100.00"), result.depAmount());
        assertEquals(new BigDecimal("900.00"), result.approvedAmount());
    }

    @Test
    void floodEnginePartIsExcludedWithoutEngineProtect() {
        boolean result = service.shouldExcludeFloodEnginePart(
                "FLOOD", "ENG-001", List.of());

        assertTrue(result);
    }

    @Test
    void floodEnginePartIsNotExcludedWithEngineProtect() {
        boolean result = service.shouldExcludeFloodEnginePart(
                "FLOOD", "ENG-001", List.of("ENGINE_PROTECT"));

        assertFalse(result);
    }

    @Test
    void nonFloodEnginePartIsNotExcluded() {
        boolean result = service.shouldExcludeFloodEnginePart(
                "COLLISION", "ENG-001", List.of());

        assertFalse(result);
    }

@Test
void shouldFindPolicyByNumber() {
    Policy policy = new Policy();
    policy.setPolicyNo("MTR-TN-778812");

    when(policyRepository.findById("MTR-TN-778812"))
            .thenReturn(Optional.of(policy));

    Policy result = service.findPolicyByNumber("MTR-TN-778812");

    assertEquals("MTR-TN-778812", result.getPolicyNo());
}

@Test
void shouldThrowWhenPolicyDoesNotExist() {
    when(policyRepository.findById("UNKNOWN"))
            .thenReturn(Optional.empty());

    assertThrows(
            PolicyNotFoundException.class,
            () -> service.findPolicyByNumber("UNKNOWN")
    );
}

@Test
void shouldReturnTrueForIdenticalReplay() throws Exception {
    String body = "{\"claimRef\":\"CLM-2026-000100\"}";

    ClaimHeader existing = new ClaimHeader();
    existing.setPayloadHash(hash(body));

    when(claimHeaderRepository.findByClaimRef("CLM-2026-000100"))
            .thenReturn(Optional.of(existing));

    assertTrue(service.isIdenticalReplay(
            "CLM-2026-000100", body
    ));
}

@Test
void shouldReturnFalseForDifferentPayload() throws Exception {
    ClaimHeader existing = new ClaimHeader();
    existing.setPayloadHash(hash("original"));

    when(claimHeaderRepository.findByClaimRef("CLM-2026-000100"))
            .thenReturn(Optional.of(existing));

    assertFalse(service.isIdenticalReplay(
            "CLM-2026-000100", "changed"
    ));
}

@Test
void shouldReturnFalseWhenClaimDoesNotExist() {
    when(claimHeaderRepository.findByClaimRef("CLM-2026-000100"))
            .thenReturn(Optional.empty());

    assertFalse(service.isIdenticalReplay(
            "CLM-2026-000100", "request"
    ));
}

@Test
void shouldCalculateLineItem() {
    LineItemRequest item = new LineItemRequest();
    item.setQuantity(1);
    item.setUnitPrice(new BigDecimal("1000.00"));
    item.setTaxPercent(18);

    LineCalculation result = service.calculateLineItem(item);

    assertEquals(new BigDecimal("180.00"), result.taxAmount());
    assertEquals(new BigDecimal("1180.00"), result.lineTotal());
}

@Test
void shouldDelegateDepreciationPercentCalculation() {
    when(depreciationRuleEngine.getDepreciationPercent(
            "METAL", 4, false
    )).thenReturn(new BigDecimal("10.00"));

    BigDecimal result = service.getDepreciationPercent(
            "METAL", 4, false
    );

    assertEquals(new BigDecimal("10.00"), result);
}

@Test
void shouldDelegateDepreciationCalculation() {
    DepreciationCalculation expected = new DepreciationCalculation(
            new BigDecimal("10.00"),
            new BigDecimal("118.00"),
            new BigDecimal("1062.00")
    );

    when(depreciationRuleEngine.calculateDepreciation(
            new BigDecimal("1180.00"),
            new BigDecimal("10.00")
    )).thenReturn(expected);

    DepreciationCalculation result = service.calculateDepreciation(
            new BigDecimal("1180.00"),
            new BigDecimal("10.00")
    );

    assertEquals(new BigDecimal("118.00"), result.depAmount());
}

@Test
void shouldExcludeFloodEnginePartWithoutEngineProtect() {
    assertTrue(service.shouldExcludeFloodEnginePart(
            "FLOOD", "ENG-001", List.of()
    ));
}

@Test
void shouldNotExcludeFloodEnginePartWithEngineProtect() {
    assertFalse(service.shouldExcludeFloodEnginePart(
            "FLOOD", "ENG-001", List.of("ENGINE_PROTECT")
    ));
}

@Test
void shouldRetrieveExistingClaim() {
    ClaimHeader claim = new ClaimHeader();
    claim.setClaimId(10L);
    claim.setClaimRef("CLM-2026-000100");
    claim.setCorrelationId("test-correlation");
    claim.setStatus("APPROVED");
    claim.setFraudScore(0);
    claim.setTotalAssessed(new BigDecimal("1000.00"));
    claim.setTotalDepreciation(BigDecimal.ZERO);
    claim.setDeductibleApplied(new BigDecimal("100.00"));
    claim.setCopayAmount(BigDecimal.ZERO);
    claim.setPayableAmount(new BigDecimal("900.00"));

    when(claimHeaderRepository.findByClaimRef("CLM-2026-000100"))
            .thenReturn(Optional.of(claim));

    when(claimFraudFlagRepository.findByClaimHeaderClaimId(10L))
            .thenReturn(List.of());

    var result = service.getClaim("CLM-2026-000100");

    assertEquals("CLM-2026-000100", result.claimRef());
    assertEquals("APPROVED", result.status());
}

@Test
void shouldThrowWhenRetrievingMissingClaim() {
    when(claimHeaderRepository.findByClaimRef("UNKNOWN"))
            .thenReturn(Optional.empty());

    assertThrows(
            IllegalArgumentException.class,
            () -> service.getClaim("UNKNOWN")
    );
}

}
