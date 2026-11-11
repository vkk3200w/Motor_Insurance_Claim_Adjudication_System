
package com.company.insurance.validation;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class ClaimHeaderValidator {

    private static final Set<String> VALID_SOURCES =
            Set.of("PORTAL", "AGENT", "PARTNER");

    public void validate(String correlationId, String sourceSystem) {

        try {
            UUID uuid = UUID.fromString(correlationId);

            if (uuid.version() != 4) {
                throw new IllegalArgumentException(
                        "Correlation ID must be UUID version 4"
                );
            }

        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid X-Correlation-Id header"
            );
        }

        if (!VALID_SOURCES.contains(sourceSystem)) {
            throw new IllegalArgumentException(
                    "Invalid X-Source-System header"
            );
        }
    }
}
