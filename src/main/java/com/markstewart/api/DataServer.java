package com.markstewart.api;

import com.markstewart.model.Claim;
import com.markstewart.model.ClaimState;

import java.util.*;

/**
 * An abstract persistence layer used by the APIServer's various handlers.
 * In an actual application this component would implement database/caching
 * mechanisms to provide this data to the API.
 * The methods in this impl are synchronized out of an abundance of caution since the Helidon web server uses
 * (virtual) threads to service requests. If we were to try to push any significant amount of load we
 * would want to overhaul this.
 */
public class DataServer {
    // A set of maps that play the part of a persistence layer
    private final Map<UUID, Claim> claimsByClaimId = new HashMap<>();
    private final Map<UUID, List<Claim>> claimsByCustomer =new HashMap<>();

    public synchronized boolean submitClaim(Claim claim) {
        // Add the claim to both maps...
        if (null != claimsByClaimId.put(claim.id(), claim)) {
            System.out.println("Replacing existing Claim with id, " + claim.id());
            // TODO It might not make sense to allow (mostly silent) updates of Claims.
            // Restrict updates to Claims that are still in RECEIVED state?
        }

        final UUID customerId = claim.policyHolder().customer().id();
        List<Claim> customerClaims = claimsByCustomer.computeIfAbsent(customerId, k -> new LinkedList<>());
        customerClaims.add(claim);

        // DEBUG:
        // System.out.println("Current size of claimsByClaimId = " + claimsByClaimId.size());
        // System.out.println("Current size of claimsByCustomer = " + claimsByCustomer.size());

        return true;
    }

    public synchronized List<Claim> findClaimsByCustomer(UUID customerId) {
        return claimsByCustomer.get(customerId);
    }

    public synchronized UpdateStatus updateClaimStatus(UUID claimId, ClaimState newState) {
        final Claim claim = claimsByClaimId.get(claimId);
        if (claim == null) {
            System.out.println("No claim found for " + claimId);
            return UpdateStatus.CLAIM_NOT_FOUND;
        }

        if (!ClaimState.allowStateTransition(claim.state(), newState)) {
            System.out.println("Illegal claim state transition");
            return UpdateStatus.BAD_STATE_TRANSITION;
        }

        final Claim updatedClaim = claim.copyWithUpdatedState(claim.id(), newState);
        System.out.println("New state for claim: " + updatedClaim.state());

        // update the maps with the new Claim
        claimsByClaimId.put(claim.id(), updatedClaim);

        // Find and replace the instance in the other map
        UUID customerId = claim.policyHolder().customer().id();
        final List<Claim> claimsByCustomer = findClaimsByCustomer(customerId);
        claimsByCustomer.remove(claim);
        claimsByCustomer.add(updatedClaim);

        return UpdateStatus.OK;
    }

}
