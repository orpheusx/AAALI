package com.markstewart.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentInfoTest extends JsonSerdeSupport {

    @Test
    public void testSerde() {
        assertDoesNotThrow(() -> {
//            PaymentInfo paymentInfo = new PaymentInfo(
//                    "First National Bank",
//                    "23k42kj3b4jh3b4",
//                    "012345678");
//
//            String expectedJson = """
//                    {
//                      "bankName" : "First National Bank",
//                      "fdicId" : "23k42kj3b4jh3b4",
//                      "routingNumber" : "012345678"
//                    }""";

            var json = mapper.writeValueAsString(examplePaymentInfo);
//            System.out.println(json);
            assertEquals(json, examplePaymentInfoJson);

            // round trip
            PaymentInfo paymentInfo1 = mapper.readValue(json, PaymentInfo.class);

            assertEquals(examplePaymentInfo, paymentInfo1);
        });
    }
}