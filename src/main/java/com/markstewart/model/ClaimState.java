package com.markstewart.model;

public enum ClaimState {
    RECEIVED, IN_REVIEW, APPROVED, DENIED;

    public static boolean allowStateTransition(ClaimState current, ClaimState next) {
        return switch (current) {
            case ClaimState c when c.equals(RECEIVED) ->
                    next.equals(IN_REVIEW);
            case ClaimState c when c.equals(IN_REVIEW) ->
                    next.equals(APPROVED) || next.equals(DENIED);
            default -> false;
        };
    }
}
