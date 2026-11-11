
package com.company.insurance.rules;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class ClaimNormalizer {

    public static String normalizeName(String name) {
        String cleaned = name.trim().replaceAll("\\s+", " ");
        String[] words = cleaned.toLowerCase().split(" ");

        StringBuilder result = new StringBuilder();

        for (String word : words) {
            result.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1))
                  .append(" ");
        }

        return result.toString().trim();
    }

    public static String normalizeRegNo(String regNo) {
    String normalized =
            regNo.replaceAll("[\\s-]", "").toUpperCase();

    if (!normalized.matches("[A-Z]{2}\\d{2}[A-Z]{1,3}\\d{4}")) {
        throw new IllegalArgumentException(
                "Invalid vehicle registration number"
        );
    }

    return normalized;
}

    public static String maskMobile(String mobile) {
        String cleaned = mobile.replace("+91", "")
                               .replaceAll("[\\s-]", "");

        return "XXXXXX" + cleaned.substring(6);
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public static LocalDateTime toUtc(OffsetDateTime dateTime) {
        return dateTime.atZoneSameInstant(ZoneOffset.UTC)
                       .toLocalDateTime();
    }

    public static int calculateVehicleAge(int manufactureYear,
                                          int incidentYear) {
        return incidentYear - manufactureYear;
    }
}
