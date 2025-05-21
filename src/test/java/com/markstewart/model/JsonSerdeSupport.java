package com.markstewart.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDate;
import java.util.UUID;

public class JsonSerdeSupport {

    public Customer exampleCustomer = new Customer(UUID.fromString(
            "f2d8c645-889f-4a98-82eb-d46527c52452"),
            "Stewart", "Mark",
            "22 Baker Street, Boston", "MA", "55555",
            "bogusemailaddress@gmail.com", LocalDate.of(1921, 12, 23),
            "123456789");

    public String exampleCustomerAsJson = """
                    {
                      "id" : "f2d8c645-889f-4a98-82eb-d46527c52452",
                      "surname" : "Stewart",
                      "givenName" : "Mark",
                      "streetAddress" : "22 Baker Street, Boston",
                      "stateOrProvince" : "MA",
                      "postalCode" : "55555",
                      "emailAddress" : "bogusemailaddress@gmail.com",
                      "dob" : [ 1901, 12, 12 ],
                      "socialInsuranceId" : "123456789"
                    }""";

    public PaymentInfo examplePaymentInfo = new PaymentInfo(
            "First National Bank",
            "23k42kj3b4jh3b4",
            "012345678");

    public String examplePaymentInfoJson = """
                    {
                      "bankName" : "First National Bank",
                      "fdicId" : "23k42kj3b4jh3b4",
                      "routingNumber" : "012345678"
                    }""";

    public String exampleInsuredOrPolicyHolderJson = """
            {
              "customer" : {
                "id" : "e145983b-4a90-4a69-a418-672e907cfeb4",
                "surname" : "Stewart",
                "givenName" : "Mark",
                "streetAddress" : "22 Baker Street, Boston",
                "stateOrProvince" : "MA",
                "postalCode" : "55555",
                "emailAddress" : "bogusemailaddress@gmail.com",
                "dob" : [ 1901, 12, 12 ],
                "socialInsuranceId" : "123456789"
              }
            }""";

    final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .registerModule(new JavaTimeModule());
}
