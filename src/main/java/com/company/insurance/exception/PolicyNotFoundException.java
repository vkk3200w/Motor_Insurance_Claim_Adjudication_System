
package com.company.insurance.exception;

public class PolicyNotFoundException extends RuntimeException {

    public PolicyNotFoundException(String policyNo) {
        super("Policy not found: " + policyNo);
    }
}
