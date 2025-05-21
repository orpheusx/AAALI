package com.markstewart.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest extends JsonSerdeSupport {

    @Test
    public void testSerde() {
        assertDoesNotThrow(() -> {
            var json = mapper.writeValueAsString(exampleCustomer);
            // assertEquals(json, exampleCustomerAsJson);

            Customer customer1 = mapper.readValue(json, Customer.class);
            assertEquals(customer1, exampleCustomer);
        });
    }
}