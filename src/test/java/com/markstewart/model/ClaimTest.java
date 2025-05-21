package com.markstewart.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClaimTest extends JsonSerdeSupport {

    @Test
    public void testSerde() {
        assertDoesNotThrow(() -> {
            Claim claim = new Claim(
                    Instant.now(),
                    UUID.fromString("7213f19f-e28d-40a8-856c-c8478bedde61"),
                    "09876543456-4432",
                    new Insured(exampleCustomer),
                    new PolicyHolder(exampleCustomer),
                    ClaimState.RECEIVED);

//            String claimJson = """
//                    {
//                      "submittedAt" : 1747781032.082495000,
//                      "id" : "7213f19f-e28d-40a8-856c-c8478bedde61",
//                      "policyId" : "09876543456-4432",
//                      "deceased" : {
//                        "customer" : {
//                          "id" : "f2d8c645-889f-4a98-82eb-d46527c52452",
//                          "surname" : "Stewart",
//                          "givenName" : "Mark",
//                          "streetAddress" : "22 Baker Street, Boston",
//                          "stateOrProvince" : "MA",
//                          "postalCode" : "55555",
//                          "emailAddress" : "bogusemailaddress@gmail.com",
//                          "dob" : [ 1901, 12, 12 ],
//                          "socialInsuranceId" : "123456789"
//                        }
//                      },
//                      "policyHolder" : {
//                        "customer" : {
//                          "id" : "f2d8c645-889f-4a98-82eb-d46527c52452",
//                          "surname" : "Stewart",
//                          "givenName" : "Mark",
//                          "streetAddress" : "22 Baker Street, Boston",
//                          "stateOrProvince" : "MA",
//                          "postalCode" : "55555",
//                          "emailAddress" : "bogusemailaddress@gmail.com",
//                          "dob" : [ 1901, 12, 12 ],
//                          "socialInsuranceId" : "123456789"
//                        }
//                      }
//                    }""";

            var asJson = mapper.writeValueAsString(claim);
            //assertEquals(claimJson, asJson);
            System.out.println(asJson);

            Claim claim1 = mapper.readValue(asJson, Claim.class);
            assertEquals(claim, claim1);

        });
    }

}