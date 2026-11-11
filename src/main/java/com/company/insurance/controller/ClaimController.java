
package com.company.insurance.controller;

import com.company.insurance.dto.request.ClaimRequest;
import com.company.insurance.dto.response.ClaimResponse;
import com.company.insurance.service.ClaimAdjudicationService;
import com.company.insurance.validation.ClaimHeaderValidator;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/claims")
public class ClaimController {

    private final ClaimAdjudicationService claimAdjudicationService;
    private final ClaimHeaderValidator headerValidator;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public ClaimController(
            ClaimHeaderValidator headerValidator,
            ClaimAdjudicationService claimAdjudicationService,
            ObjectMapper objectMapper,
            Validator validator) {

        this.headerValidator = headerValidator;
        this.claimAdjudicationService = claimAdjudicationService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping("/adjudicate")
    public ResponseEntity<ClaimResponse> adjudicateClaim(
            @RequestBody String rawRequestBody,
            @RequestHeader("X-Correlation-Id") String correlationId,
            @RequestHeader("X-Source-System") String sourceSystem)
            throws Exception {

        headerValidator.validate(correlationId, sourceSystem);

        ClaimRequest request = objectMapper.readValue(
                rawRequestBody, ClaimRequest.class);

        Set<ConstraintViolation<ClaimRequest>> violations =
                validator.validate(request);

        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(
                    violations.iterator().next().getMessage());
        }

        boolean replay = claimAdjudicationService.isIdenticalReplay(
        request.getClaimRef(), rawRequestBody);

ClaimResponse response = claimAdjudicationService.processClaim(
        request, correlationId, sourceSystem, rawRequestBody);

return ResponseEntity
        .status(replay ? HttpStatus.OK : HttpStatus.CREATED)
        .body(response);
    }
    @GetMapping("/{claimRef}")
public ResponseEntity<ClaimResponse> getClaim(
        @PathVariable String claimRef) {

    ClaimResponse response =
            claimAdjudicationService.getClaim(claimRef);

    return ResponseEntity.ok(response);
}
}
