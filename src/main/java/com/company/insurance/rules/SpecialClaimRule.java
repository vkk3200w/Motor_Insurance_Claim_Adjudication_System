
package com.company.insurance.rules;

public class SpecialClaimRule {

    public static boolean shouldExcludeFloodEnginePart(
            String incidentType,
            String partCode,
            boolean engineProtect
    ) {
        return "FLOOD".equalsIgnoreCase(incidentType)
                && partCode != null
                && partCode.startsWith("ENG-")
                && !engineProtect;
    }

    public static boolean isTheftClaim(String incidentType) {
        return "THEFT".equalsIgnoreCase(incidentType);
    }
}
