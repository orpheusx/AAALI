package com.markstewart.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.UUIDSerializer;


import java.time.LocalDate;
import java.util.UUID;

/**
 * This is the basic encapsulation of the information needed for
 * various customer types. We use composition of immutable types
 * rather than inheritance.
 * @param id
 * @param surname
 * @param givenName
 * @param streetAddress
 * @param stateOrProvince
 * @param postalCode
 * @param emailAddress
 * @param dob
 * @param socialInsuranceId
 * @see Beneficiary, Insured, PolicyHolder
 */
public record Customer (
        @JsonSerialize(using= UUIDSerializer.class) UUID id,
        String surname,
        String givenName,
        String streetAddress,
        String stateOrProvince,
        String postalCode,
        // optional
        String emailAddress,
        LocalDate dob,
        String socialInsuranceId)
{}
