package com.markstewart.model;

/**
 * For the purposes of the evaluation I'm simplifying the ways in which we represent this.
 * @param bankName
 * @param fdicId
 * @param routingNumber
 */
public record PaymentInfo(
        String bankName,
        String fdicId,
        String routingNumber // NB: probably better ways to represent this
) {}
