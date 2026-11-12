
package com.company.insurance.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    // Helper method for creating MethodParameter
    private MethodParameter getMethodParameter() throws Exception {
        return new MethodParameter(
                GlobalExceptionHandlerTest.class
                        .getDeclaredMethod("dummyMethod", String.class), 0);
    }

    private void dummyMethod(String value) {
    }

    @Test
    void shouldHandlePolicyNotFound() {

        var response = handler.handlePolicyNotFound(
                new PolicyNotFoundException("POL-123"));

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Not Found", response.getBody().get("error"));
        assertTrue(response.getBody().containsKey("timestamp"));
    }

    @Test
    void shouldHandleInvalidArgument() {

        var response = handler.handleInvalidRequest(
                new IllegalArgumentException("Invalid input"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Invalid input", response.getBody().get("message"));
    }

    @Test
    void shouldHandleClaimNotFound() {

        var response = handler.handleInvalidRequest(
                new IllegalArgumentException("CLAIM_NOT_FOUND"));

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Claim not found", response.getBody().get("message"));
    }

    @Test
    void shouldHandleMissingHeader() throws Exception {

        var exception = new MissingRequestHeaderException(
                "X-Correlation-Id", getMethodParameter());

        var response = handler.handleMissingHeader(exception);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(
                "Missing required header: X-Correlation-Id",
                response.getBody().get("message"));
    }

    @Test
    void shouldHandleValidationErrors() throws Exception {

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "claimRequest");

        bindingResult.addError(new FieldError(
                "claimRequest", "claimRef", "Claim reference is required"));

        // Duplicate field checks the merge function.
        bindingResult.addError(new FieldError(
                "claimRequest", "claimRef", "Invalid claim reference"));

        bindingResult.addError(new FieldError(
                "claimRequest", "policyNo", "Policy number is required"));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(
                        getMethodParameter(), bindingResult);

        var response = handler.handleValidationErrors(exception);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Request validation failed",
                response.getBody().get("message"));

        Map<?, ?> errors = (Map<?, ?>)
                response.getBody().get("fieldErrors");

        assertEquals(2, errors.size());
        assertEquals("Claim reference is required",
                errors.get("claimRef"));
        assertEquals("Policy number is required",
                errors.get("policyNo"));
    }

    @Test
    void shouldHandleInvalidJson() {

        var inputMessage = new MockHttpInputMessage(new byte[0]);

        var exception = new HttpMessageNotReadableException(
                "Invalid JSON", inputMessage);

        var response = handler.handleInvalidJson(exception);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid request JSON",
                response.getBody().get("message"));
    }

    @Test
    void shouldHandleClaimLockedConflict() {

        var response = handler.handleConflict(
                new IllegalStateException("CLAIM_LOCKED"));

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Conflict", response.getBody().get("error"));
        assertEquals("Approved claim cannot be modified",
                response.getBody().get("message"));
    }

    @Test
    void shouldHandleOtherIllegalStateException() {

        var response = handler.handleConflict(
                new IllegalStateException("Unexpected state"));

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Internal Server Error",
                response.getBody().get("error"));
    }

    @Test
    void shouldHandleGeneralException() {

        var response = handler.handleGeneralError(
                new RuntimeException("Unexpected failure"));

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Internal Server Error",
                response.getBody().get("error"));
        assertEquals("An unexpected error occurred",
                response.getBody().get("message"));
        assertTrue(response.getBody().containsKey("timestamp"));
    }
}
