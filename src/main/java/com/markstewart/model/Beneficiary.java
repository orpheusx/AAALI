package com.markstewart.model;

public record Beneficiary(
        Customer customer,
        PaymentInfo paymentInfo,
        int percentageOfBenefit // portion of the benefit paid to this customer
) {}
