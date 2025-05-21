package com.markstewart.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PolicyHolderTest extends JsonSerdeSupport {

    @Test
    public void testSerde() {
        assertDoesNotThrow(() -> {

            PolicyHolder PolicyHolder = new PolicyHolder(exampleCustomer);

            var json = mapper.writeValueAsString(PolicyHolder);
            //System.out.println(json);
            //assertEquals(json, exampleInsuredOrPolicyHolderJson);

            // round trip
            PolicyHolder insured1 = mapper.readValue(json, PolicyHolder.class);

            assertEquals(PolicyHolder, insured1);
        });
    }
}