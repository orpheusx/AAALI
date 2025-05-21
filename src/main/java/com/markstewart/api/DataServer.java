package com.markstewart.api;

import com.markstewart.model.Claim;
import com.markstewart.model.ClaimState;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An abstract persistence layer used by the APIServer's various handlers.
 * In an actual application this component would implement database/caching
 * mechanisms to provide this data to the API.
 * The methods in this impl are synchronized out of an abundance of caution since the Helidon web server uses
 * (virtual) threads to service requests. If we were to try to push any significant amount of load we
 * would want to overhaul this to reduce locking.
 */
public class DataServer {
    // A set of maps that play the part of a persistence layer
    protected final Map<UUID, Claim> claimsByClaimId = new HashMap<>();
    protected final Map<UUID, List<Claim>> claimsByCustomer = new HashMap<>();
    protected final Map<String, Set<UUID>> claimIdsByPolicyId = new HashMap<>();


    /**
     * Submit a new claim (no file upload needed, just metadata)
     * @param claim the Claim being created
     */
    public synchronized void submitClaim(Claim claim) {
        // Add the claim to both maps...
        if (null != claimsByClaimId.put(claim.id(), claim)) {
            System.out.println("Replacing existing Claim with id, " + claim.id());
            // TODO It might not make sense to allow (mostly silent) updates of Claims.
            // Restrict updates to Claims that are still in RECEIVED state?
        }

        final UUID customerId = claim.policyHolder().customer().id();
        List<Claim> customerClaims = claimsByCustomer.computeIfAbsent(customerId, k -> new LinkedList<>());
        customerClaims.add(claim);

        // Keep track of claims made by policy. This explicitly allows for duplicates but makes it easy-ish to keep track of them.
        final Set<UUID> claimsForPolicy = claimIdsByPolicyId.computeIfAbsent(claim.policyId(), k -> new LinkedHashSet<>());
        claimsForPolicy.add(claim.id());

        // DEBUG:
        // System.out.println("Current size of claimsByClaimId = " + claimsByClaimId.size());
        // System.out.println("Current size of claimsByCustomer = " + claimsByCustomer.size());

    }

    /**
     * Retrieve all claims for a given customer
     * @param customerId the id the customer/policyholder
     * @return the list of claims made by the given customer
     */
    public synchronized List<Claim> findClaimsByCustomer(UUID customerId) {
        return claimsByCustomer.get(customerId);
    }

    /**
     *  Update a claim status (approve, deny)
     *  I've added two additional states.
     * @param claimId the id of the claim changing states
     * @param newState the new state of the given claim
     * @return the enum representing the outcome of the operation
     */
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

    /**
     * Flag and retrieve potential duplicate claims.
     * Per Chester's email: "Claims are generally made against a policy. Assuming that a claim can at most be filed against
     * a single policy, for this task, the duplicate check can be assumed to be against a single policy (or single customer,
     * if you wish to make that 1:1 assumption – but please document that if the case)." My implementation assumes
     * duplicates are Claims that reference the same policy id. Hope I understood this correctly.
     *
     * @param policyId
     * @return the list of claims that reference the same policy id
     */
    public synchronized List<Claim> detectDuplicateClaimsForPolicy(String policyId) {
        final Set<UUID> claimIds = claimIdsByPolicyId.get(policyId);
        System.out.println("Found " + claimIds.size() + " claims with policy id, " + policyId);
        return claimIds.stream().map(claimsByClaimId::get).collect(Collectors.toList());
    }

}
