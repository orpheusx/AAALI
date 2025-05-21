package com.markstewart.api;

import com.markstewart.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Some simple unit tests checking DataServer's public methods.
 */
class DataServerTest extends JsonSerdeSupport {

    private DataServer dataServer;

    private final UUID claimId1 = UUID.fromString("7213f19f-e28d-40a8-856c-c8478bedde61");
    private final UUID claimId2 = UUID.fromString("e3f6f3dc-ca9f-4dea-b87f-d48ba6326c35");

    private final Claim claim1 = new Claim(
            Instant.now(),
            claimId1,
            "09876543456-4432",
            new Insured(exampleCustomer),
            new PolicyHolder(exampleCustomer),
            ClaimState.RECEIVED);

    // The second claim differs only by its claimId
    private final Claim claim2 = new Claim(
            Instant.now(),
            claimId2,
            "09876543456-4432",
            new Insured(exampleCustomer),
            new PolicyHolder(exampleCustomer),
            ClaimState.RECEIVED);


    @BeforeEach
    void setUp() {
        dataServer = new DataServer();
    }

    @Test
    void submitClaim() {

        dataServer.submitClaim(claim1);

        Claim storedClaim = dataServer.claimsByClaimId.get(claimId1);
        assertEquals(claim1, storedClaim);

    }

    @Test
    void findClaimsByCustomer() {
        dataServer.submitClaim(claim1);

        final List<Claim> claimsByCustomer = dataServer.findClaimsByCustomer(claim1.policyHolder().customer().id());
        assertEquals(1, claimsByCustomer.size());
        assertEquals(claim1, claimsByCustomer.getFirst());

    }

    @Test
    void updateClaimStatus() {
        dataServer.submitClaim(claim1);

        dataServer.updateClaimStatus(claimId1, ClaimState.IN_REVIEW);
        assertEquals(ClaimState.IN_REVIEW, dataServer.claimsByClaimId.get(claimId1).state());

        dataServer.updateClaimStatus(claimId1, ClaimState.APPROVED);
        assertEquals(ClaimState.APPROVED, dataServer.claimsByClaimId.get(claimId1).state());

        dataServer.updateClaimStatus(claimId1, ClaimState.RECEIVED); // illegal transition so state remains unchangess
        assertEquals(ClaimState.APPROVED, dataServer.claimsByClaimId.get(claimId1).state());

    }

    @Test
    void detectDuplicateClaimsForPolicy() {
        dataServer.submitClaim(claim1);
        dataServer.submitClaim(claim2);
        assertEquals(2, dataServer.claimsByClaimId.size());

        final List<Claim> duplicateClaims = dataServer.detectDuplicateClaimsForPolicy(claim1.policyId());
        assertEquals(2, duplicateClaims.size());
    }
}