
package com.company.insurance.service;

import com.company.insurance.dto.request.*;
import com.company.insurance.entity.*;
import com.company.insurance.repository.*;
import com.company.insurance.rules.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimAdjudicationProcessTest {

    @Mock PolicyRepository policyRepository;
    @Mock DepreciationRuleRepository depreciationRuleRepository;
    @Mock ClaimHeaderRepository claimHeaderRepository;
    @Mock ClaimLineRepository claimLineRepository;
    @Mock ClaimFraudFlagRepository claimFraudFlagRepository;
    @Mock ClaimAuditRepository claimAuditRepository;
    @Mock DepreciationRuleEngine depreciationRuleEngine;
    @Mock FraudDetectionRule fraudDetectionRule;

    @InjectMocks
    ClaimAdjudicationService service;

    private ClaimRequest createRequest() {
        ClaimRequest request = new ClaimRequest();
        request.setClaimRef("CLM-2026-000100");
        request.setSubmittedAt(
            OffsetDateTime.parse("2026-10-15T10:00:00+05:30"));

        PolicyRequest policy = new PolicyRequest();
        policy.setPolicyNo("MTR-TN-778812");
        policy.setProductCode("COMP");
        policy.setAddOns(List.of());
        request.setPolicy(policy);

        VehicleRequest vehicle = new VehicleRequest();
        vehicle.setRegNo("TN01AB1234");
        vehicle.setMake("Honda");
        vehicle.setModel("City");
        vehicle.setManufactureYear(2022);
        vehicle.setFuelType("PETROL");
        request.setVehicle(vehicle);

        ContactRequest contact = new ContactRequest();
        contact.setMobile("9876543210");
        contact.setEmail("test@example.com");

        ClaimantRequest claimant = new ClaimantRequest();
        claimant.setName("Test Customer");
        claimant.setRelationship("SELF");
        claimant.setContact(contact);
        request.setClaimant(claimant);

        IncidentLocationRequest location =
            new IncidentLocationRequest();
        location.setCity("Chennai");
        location.setState("Tamil Nadu");
        location.setPincode("600001");

        IncidentRequest incident = new IncidentRequest();
        incident.setDate(LocalDate.of(2026, 10, 10));
        incident.setTime(LocalTime.of(14, 30));
        incident.setType("COLLISION");
        incident.setLocation(location);
        incident.setPoliceReported(true);
        incident.setFirNumber("FIR123");
        request.setIncident(incident);

        GarageRequest garage = new GarageRequest();
        garage.setGarageCode("GAR-001");
        garage.setNetworkGarage(true);
        request.setGarage(garage);

        request.setPriorClaimsInLast12Months(2);

        LineItemRequest line = new LineItemRequest();
        line.setLineNo(1);
        line.setPartCode("BODY-001");
        line.setCategory("METAL");
        line.setDescription("Body panel");
        line.setQuantity(1);
        line.setUnitPrice(new BigDecimal("10000.00"));
        line.setTaxPercent(18);
        request.setLineItems(List.of(line));

        return request;
    }

    private Policy createPolicy() {
        Policy policy = new Policy();
        policy.setPolicyNo("MTR-TN-778812");
        policy.setProductCode("COMP");
        policy.setStartDate(LocalDate.of(2026, 1, 1));
        policy.setEndDate(LocalDate.of(2026, 12, 31));
        policy.setSumInsured(new BigDecimal("650000.00"));
        policy.setDeductible(new BigDecimal("2000.00"));
        policy.setNcbPercent(25);
        policy.setStatus("ACTIVE");
        return policy;
    }

    @Test
    void shouldSuccessfullyProcessNewClaim() {

        ClaimRequest request = createRequest();
        Policy policy = createPolicy();

        when(policyRepository.findById("MTR-TN-778812"))
            .thenReturn(Optional.of(policy));

        when(claimHeaderRepository.findByClaimRef(
                "CLM-2026-000100"))
            .thenReturn(Optional.empty());

        when(depreciationRuleEngine.getDepreciationPercent(
                "METAL", 4, false))
            .thenReturn(new BigDecimal("10.00"));

        when(depreciationRuleEngine.calculateDepreciation(
                new BigDecimal("11800.00"),
                new BigDecimal("10.00")))
            .thenReturn(new DepreciationCalculation(
                new BigDecimal("10.00"),
                new BigDecimal("1180.00"),
                new BigDecimal("10620.00")));

        when(claimHeaderRepository.save(any(ClaimHeader.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        String rawBody = "{\"claimRef\":\"CLM-2026-000100\"}";

        var response = service.processClaim(
            request,
            "test-correlation",
            "PORTAL",
            rawBody
        );

        assertEquals("CLM-2026-000100", response.claimRef());
        assertEquals("APPROVED", response.status());
        assertEquals(25, response.fraudScore());
        assertEquals(
            new BigDecimal("8620.00"),
            response.payableAmount()
        );

        assertEquals(0, policy.getNcbPercent());
        assertEquals(
            LocalDate.of(2026, 10, 10),
            policy.getLastClaimDate()
        );

        verify(policyRepository).save(policy);
        verify(claimHeaderRepository).save(any(ClaimHeader.class));
        verify(claimLineRepository).save(any(ClaimLine.class));
        verify(claimFraudFlagRepository).save(any(ClaimFraudFlag.class));
        verify(claimAuditRepository).save(any(ClaimAudit.class));
    }

@Test
void shouldCheckPolicyCoverageForValidPolicy() {

    ClaimRequest request = createRequest();
    Policy policy = createPolicy();

    when(policyRepository.findById("MTR-TN-778812"))
        .thenReturn(Optional.of(policy));

    String result = service.checkPolicyCoverage(request);

    assertNull(result);
    verify(policyRepository).findById("MTR-TN-778812");
}

@Test
void shouldReturnCorrectPointsForEveryFraudRule() throws Exception {

    Method method = ClaimAdjudicationService.class
        .getDeclaredMethod("getFraudPoints", String.class);

    method.setAccessible(true);

    assertEquals(30, method.invoke(service, "F01"));
    assertEquals(40, method.invoke(service, "F02"));
    assertEquals(25, method.invoke(service, "F03"));
    assertEquals(15, method.invoke(service, "F04"));
    assertEquals(10, method.invoke(service, "F05"));
    assertEquals(10, method.invoke(service, "F06"));
    assertEquals(0, method.invoke(service, "UNKNOWN"));
}

@Test
void shouldReturnExistingClaimForIdenticalReplay() throws Exception {

    String rawBody = "{\"claimRef\":\"CLM-2026-000100\"}";

    ClaimRequest request = createRequest();
    Policy policy = createPolicy();

    ClaimHeader existingClaim = new ClaimHeader();
    existingClaim.setClaimId(1L);
    existingClaim.setClaimRef("CLM-2026-000100");
    existingClaim.setCorrelationId("old-correlation");
    existingClaim.setStatus("APPROVED");
    existingClaim.setStatusReason("Claim approved");
    existingClaim.setFraudScore(25);
    existingClaim.setTotalAssessed(new BigDecimal("11800.00"));
    existingClaim.setTotalDepreciation(new BigDecimal("1180.00"));
    existingClaim.setDeductibleApplied(new BigDecimal("2000.00"));
    existingClaim.setCopayAmount(BigDecimal.ZERO);
    existingClaim.setPayableAmount(new BigDecimal("8620.00"));

    // Generate the same SHA-256 hash used by the service.
    java.security.MessageDigest digest =
        java.security.MessageDigest.getInstance("SHA-256");

    String hash = java.util.HexFormat.of().formatHex(
        digest.digest(rawBody.getBytes(
            java.nio.charset.StandardCharsets.UTF_8))
    );

    existingClaim.setPayloadHash(hash);

    ClaimFraudFlag flag = new ClaimFraudFlag();
    flag.setRuleCode("F03");

    when(policyRepository.findById("MTR-TN-778812"))
        .thenReturn(Optional.of(policy));

    when(claimHeaderRepository.findByClaimRef("CLM-2026-000100"))
        .thenReturn(Optional.of(existingClaim));

    when(claimFraudFlagRepository.findByClaimHeaderClaimId(1L))
        .thenReturn(List.of(flag));

    var response = service.processClaim(
        request, "new-correlation", "PORTAL", rawBody
    );

    assertEquals("APPROVED", response.status());
    assertEquals("old-correlation", response.correlationId());
    assertEquals(25, response.fraudScore());
    assertEquals(List.of("F03"), response.fraudFlags());

    verify(claimHeaderRepository, never()).save(any());
    verify(claimLineRepository, never()).save(any());
    verify(claimAuditRepository, never()).save(any());
}

@Test
void shouldRejectChangedPayloadForApprovedClaim() {

    ClaimRequest request = createRequest();
    Policy policy = createPolicy();

    ClaimHeader existingClaim = new ClaimHeader();
    existingClaim.setClaimId(1L);
    existingClaim.setStatus("APPROVED");
    existingClaim.setPayloadHash("old-payload-hash");

    when(policyRepository.findById("MTR-TN-778812"))
        .thenReturn(Optional.of(policy));

    when(claimHeaderRepository.findByClaimRef("CLM-2026-000100"))
        .thenReturn(Optional.of(existingClaim));

    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> service.processClaim(
            request,
            "new-correlation",
            "PORTAL",
            "{\"claimRef\":\"different-payload\"}"
        )
    );

    assertEquals("CLAIM_LOCKED", exception.getMessage());

    verify(claimHeaderRepository, never()).save(any());
    verify(claimLineRepository, never()).save(any());
    verify(claimAuditRepository, never()).save(any());
}

@Test
void shouldResubmitClaimWhenExistingClaimIsNotApproved() {

    ClaimRequest request = createRequest();
    Policy policy = createPolicy();

    ClaimHeader existingClaim = new ClaimHeader();
    existingClaim.setClaimId(10L);
    existingClaim.setClaimRef("CLM-2026-000100");
    existingClaim.setStatus("REFERRED");
    existingClaim.setPayloadHash("old-hash");

    when(policyRepository.findById("MTR-TN-778812"))
        .thenReturn(Optional.of(policy));

    when(claimHeaderRepository.findByClaimRef("CLM-2026-000100"))
        .thenReturn(Optional.of(existingClaim));

    when(depreciationRuleEngine.getDepreciationPercent(
            "METAL", 4, false))
        .thenReturn(new BigDecimal("10.00"));

    when(depreciationRuleEngine.calculateDepreciation(
            new BigDecimal("11800.00"),
            new BigDecimal("10.00")))
        .thenReturn(new DepreciationCalculation(
            new BigDecimal("10.00"),
            new BigDecimal("1180.00"),
            new BigDecimal("10620.00")));

    when(claimHeaderRepository.save(any(ClaimHeader.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response = service.processClaim(
        request,
        "new-correlation",
        "PORTAL",
        "{\"claimRef\":\"updated-payload\"}"
    );

    assertEquals("CLM-2026-000100", response.claimRef());
    assertEquals("APPROVED", response.status());

    verify(claimLineRepository).deleteByClaimHeaderClaimId(10L);
    verify(claimFraudFlagRepository).deleteByClaimHeaderClaimId(10L);

    verify(claimLineRepository).flush();
    verify(claimFraudFlagRepository).flush();

    verify(claimHeaderRepository).save(existingClaim);
    verify(claimAuditRepository).save(
        argThat(audit ->
            "RESUBMITTED".equals(audit.getEventType())
        )
    );
}

@Test
void shouldProcessTheftClaimWithoutPayingLineItems() {

    ClaimRequest request = createRequest();
    request.getIncident().setType("THEFT");

    Policy policy = createPolicy();

    when(policyRepository.findById("MTR-TN-778812"))
        .thenReturn(Optional.of(policy));

    when(claimHeaderRepository.findByClaimRef("CLM-2026-000100"))
        .thenReturn(Optional.empty());

    when(depreciationRuleEngine.getDepreciationPercent(
            "METAL", 4, false))
        .thenReturn(new BigDecimal("10.00"));

    when(depreciationRuleEngine.calculateDepreciation(
            new BigDecimal("11800.00"),
            new BigDecimal("10.00")))
        .thenReturn(new DepreciationCalculation(
            new BigDecimal("10.00"),
            new BigDecimal("1180.00"),
            new BigDecimal("10620.00")));

    when(claimHeaderRepository.save(any(ClaimHeader.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response = service.processClaim(
        request, "corr-theft", "PORTAL", "{\"claim\":\"theft\"}"
    );

    assertEquals("REFERRED", response.status());
    assertEquals(new BigDecimal("648000.00"), response.payableAmount());

    verify(claimLineRepository).save(argThat(line ->
        "EXCLUDED".equals(line.getLineStatus())
            && "THEFT_CLAIM".equals(line.getExclusionReason())
            && BigDecimal.ZERO.compareTo(line.getApprovedAmount()) == 0
    ));
}

@Test
void shouldExcludeEnginePartForFloodWithoutEngineProtect() {

    ClaimRequest request = createRequest();
    request.getIncident().setType("FLOOD");

    request.getLineItems().get(0).setPartCode("ENG-001");
    request.getLineItems().get(0).setCategory("METAL");

    Policy policy = createPolicy();

    when(policyRepository.findById("MTR-TN-778812"))
        .thenReturn(Optional.of(policy));

    when(claimHeaderRepository.findByClaimRef("CLM-2026-000100"))
        .thenReturn(Optional.empty());

    when(depreciationRuleEngine.getDepreciationPercent(
            "METAL", 4, false))
        .thenReturn(new BigDecimal("10.00"));

    when(depreciationRuleEngine.calculateDepreciation(
            any(BigDecimal.class), any(BigDecimal.class)))
        .thenReturn(new DepreciationCalculation(
            new BigDecimal("10.00"),
            new BigDecimal("1180.00"),
            new BigDecimal("10620.00")));

    when(claimHeaderRepository.save(any(ClaimHeader.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    service.processClaim(
        request, "corr-flood", "PORTAL", "{\"claim\":\"flood\"}"
    );

    verify(claimLineRepository).save(argThat(line ->
        "EXCLUDED".equals(line.getLineStatus())
            && "FLOOD_ENGINE_EXCLUSION".equals(line.getExclusionReason())
    ));
}


@Test
void shouldNotExcludeEnginePartForFloodWithEngineProtect() {

    ClaimRequest request = createRequest();
    request.getIncident().setType("FLOOD");
    request.getPolicy().setAddOns(List.of("ENGINE_PROTECT"));

    request.getLineItems().get(0).setPartCode("ENG-001");

    Policy policy = createPolicy();

    when(policyRepository.findById("MTR-TN-778812"))
        .thenReturn(Optional.of(policy));

    when(claimHeaderRepository.findByClaimRef("CLM-2026-000100"))
        .thenReturn(Optional.empty());

    when(depreciationRuleEngine.getDepreciationPercent(
            "METAL", 4, false))
        .thenReturn(new BigDecimal("10.00"));

    when(depreciationRuleEngine.calculateDepreciation(
            any(BigDecimal.class), any(BigDecimal.class)))
        .thenReturn(new DepreciationCalculation(
            new BigDecimal("10.00"),
            new BigDecimal("1180.00"),
            new BigDecimal("10620.00")));

    when(claimHeaderRepository.save(any(ClaimHeader.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    service.processClaim(
        request, "corr-flood-protected", "PORTAL",
        "{\"claim\":\"flood-protected\"}"
    );

    verify(claimLineRepository).save(argThat(line ->
        "PAYABLE".equals(line.getLineStatus())
            && line.getExclusionReason() == null
    ));
}




}
