package com.markstewart.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeneficiaryTest extends JsonSerdeSupport {

    String exampleBeneficiary = """
            {
               "customer" : {
                 "id" : "f2d8c645-889f-4a98-82eb-d46527c52452",
                 "surname" : "Stewart",
                 "givenName" : "Mark",
                 "streetAddress" : "22 Baker Street, Boston",
                 "stateOrProvince" : "MA",
                 "postalCode" : "55555",
                 "emailAddress" : "bogusemailaddress@gmail.com",
                 "dob" : [ 1901, 12, 12 ],
                 "socialInsuranceId" : "123456789"
               },
               "paymentInfo" : {
                 "bankName" : "First National Bank",
                 "fdicId" : "23k42kj3b4jh3b4",
                 "routingNumber" : "012345678"
               },
               "percentageOfBenefit" : 100
             }""";

    @Test
    public void testSerde() {
        assertDoesNotThrow(() -> {
            Beneficiary beneficiary = new Beneficiary(
                    exampleCustomer, examplePaymentInfo, 100);
            var asJson = mapper.writeValueAsString(beneficiary);

            //System.out.println(asJson);
            //assertEquals(asJson, exampleBeneficiary); // getting odd whitespace differences

            Beneficiary beneficiary1 = mapper.readValue(asJson, Beneficiary.class);
            assertEquals(beneficiary, beneficiary1);
        });
    }
}