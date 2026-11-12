
package com.company.insurance.controller;

import com.company.insurance.dto.request.ClaimRequest;
import com.company.insurance.dto.response.ClaimResponse;
import com.company.insurance.service.ClaimAdjudicationService;
import com.company.insurance.validation.ClaimHeaderValidator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.company.insurance.exception.GlobalExceptionHandler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Set;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClaimControllerTest {

    @Mock
    private ClaimAdjudicationService service;

    @Mock
    private ClaimHeaderValidator headerValidator;

    @Mock
    private Validator validator;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
void setUp() {

    ClaimController controller = new ClaimController(
            headerValidator,
            service,
            objectMapper,
            validator
    );

    mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
}

    private ClaimResponse createResponse() {

    return new ClaimResponse(
            "CLM-2026-000100",
            "test-correlation",
            "APPROVED",
            "Claim approved",
            25,
            new BigDecimal("11800.00"),
            new BigDecimal("1180.00"),
            new BigDecimal("2000.00"),
            BigDecimal.ZERO,
            new BigDecimal("8620.00"),
            List.of()
    );
}

    @Test
    void shouldCreateNewClaim() throws Exception {

        String json = "{\"claimRef\":\"CLM-2026-000100\"}";

        when(validator.validate(any(ClaimRequest.class)))
                .thenReturn(Set.of());

        when(service.isIdenticalReplay(
                eq("CLM-2026-000100"), eq(json)))
                .thenReturn(false);

        when(service.processClaim(
                any(ClaimRequest.class),
                eq("test-correlation"),
                eq("PORTAL"),
                eq(json)))
                .thenReturn(createResponse());

        mockMvc.perform(post("/api/v1/claims/adjudicate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Correlation-Id", "test-correlation")
                        .header("X-Source-System", "PORTAL")
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.claimRef")
                        .value("CLM-2026-000100"))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(headerValidator).validate(
                "test-correlation", "PORTAL");
    }

    @Test
    void shouldReturnOkForIdenticalReplay() throws Exception {

        String json = "{\"claimRef\":\"CLM-2026-000100\"}";

        when(validator.validate(any(ClaimRequest.class)))
                .thenReturn(Set.of());

        when(service.isIdenticalReplay(
                eq("CLM-2026-000100"), eq(json)))
                .thenReturn(true);

        when(service.processClaim(
                any(ClaimRequest.class),
                anyString(),
                anyString(),
                eq(json)))
                .thenReturn(createResponse());

        mockMvc.perform(post("/api/v1/claims/adjudicate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Correlation-Id", "test-correlation")
                        .header("X-Source-System", "PORTAL")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claimRef")
                        .value("CLM-2026-000100"));

        verify(service).processClaim(
                any(ClaimRequest.class),
                anyString(),
                anyString(),
                eq(json));
    }

    @Test
    void shouldRejectRequestWithValidationErrors() throws Exception {

        String json = "{\"claimRef\":\"CLM-2026-000100\"}";

        @SuppressWarnings("unchecked")
        ConstraintViolation<ClaimRequest> violation =
                mock(ConstraintViolation.class);

        when(violation.getMessage())
                .thenReturn("Invalid claim request");

        when(validator.validate(any(ClaimRequest.class)))
                .thenReturn(Set.of(violation));

        mockMvc.perform(post("/api/v1/claims/adjudicate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Correlation-Id", "test-correlation")
                        .header("X-Source-System", "PORTAL")
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(service, never()).processClaim(
                any(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldRejectRequestWhenCorrelationHeaderIsMissing()
            throws Exception {

        mockMvc.perform(post("/api/v1/claims/adjudicate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Source-System", "PORTAL")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRetrieveClaimSuccessfully() throws Exception {

        when(service.getClaim("CLM-2026-000100"))
                .thenReturn(createResponse());

        mockMvc.perform(get("/api/v1/claims/CLM-2026-000100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claimRef")
                        .value("CLM-2026-000100"))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(service).getClaim("CLM-2026-000100");
    }
}
