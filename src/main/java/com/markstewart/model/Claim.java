package com.markstewart.model;

import java.time.Instant;
import java.util.UUID;

/**
 * The set of data required to submit an insurance claim.
 * We assume that the input has been validated for format.
 *
 * @param submittedAt the timestamp of the initial filing
 * @param id of the claim itself
 * @param policyId using
 * @param deceased the insured customer
 * @param policyHolder the customer holding the policy (often the same as the deceased)
 */
public record Claim(
        Instant submittedAt,
        UUID id,
        String policyId, // assumes format validation at the customer input tier
        Insured deceased,
        PolicyHolder policyHolder,
        ClaimState state)
{
    public Claim copyWithUpdatedState(UUID id, ClaimState claimState) {
        return new Claim(this.submittedAt, id, this.policyId, this.deceased,
                this.policyHolder, claimState);
    }

}

