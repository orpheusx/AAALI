package com.markstewart.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InsuredTest extends JsonSerdeSupport {

    @Test
    public void testSerde() {
        assertDoesNotThrow(() -> {

            Insured insured = new Insured(exampleCustomer);

            var json = mapper.writeValueAsString(insured);
            //System.out.println(json);
            //assertEquals(json, exampleInsuredOrPolicyHolderJson);

            // round trip
            Insured insured1 = mapper.readValue(json, Insured.class);

            assertEquals(insured, insured1);
        });
    }
}