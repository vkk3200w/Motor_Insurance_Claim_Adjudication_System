
package com.company.insurance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 - Policy not found
    @ExceptionHandler(PolicyNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePolicyNotFound(
            PolicyNotFoundException exception) {

        Map<String, Object> response = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 404,
                "error", "Not Found",
                "message", exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // 400 - Invalid headers or arguments
    
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<Map<String, Object>> handleInvalidRequest(
        IllegalArgumentException exception) {

    // Claim not found - 404
    if ("CLAIM_NOT_FOUND".equals(exception.getMessage())) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "status", 404,
                        "error", "Not Found",
                        "message", "Claim not found"
                ));
    }

    // Other invalid arguments - 400
    return ResponseEntity.badRequest()
            .body(Map.of(
                    "status", 400,
                    "error", "Bad Request",
                    "message", exception.getMessage()
            ));
}


    // 400 - Missing headers
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, Object>> handleMissingHeader(
            MissingRequestHeaderException exception) {

        Map<String, Object> response = Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", "Missing required header: "
                        + exception.getHeaderName()
        );

        return ResponseEntity.badRequest().body(response);
    }

    // 400 - DTO validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (first, second) -> first
                ));

        Map<String, Object> response = Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", "Request validation failed",
                "fieldErrors", errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    // 400 - Invalid JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJson(
            HttpMessageNotReadableException exception) {

        return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", "Invalid request JSON"
        ));
    }

    // 409 - Approved claim cannot be modified
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(
            IllegalStateException exception) {

        if ("CLAIM_LOCKED".equals(exception.getMessage())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "status", 409,
                    "error", "Conflict",
                    "message", "Approved claim cannot be modified"
            ));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "status", 500,
                        "error", "Internal Server Error",
                        "message", "Claim processing failed"
                ));
    }

    // 500 - Unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralError(
            Exception exception) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "status", 500,
                        "error", "Internal Server Error",
                        "message", "An unexpected error occurred"
                ));
    }
}
