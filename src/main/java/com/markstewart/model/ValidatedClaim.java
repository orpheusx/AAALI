package com.markstewart.model;

import java.time.Instant;

/**
 * A Claim that has been validated by confirming that the entities it contains
 * are known and correct. For example, that the policy exists and is in good standing, etc.
 * @param claim
 * @param validatedAt
 */
public record ValidatedClaim(Claim claim,
                             Instant validatedAt) {}
