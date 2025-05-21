package com.markstewart.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.markstewart.model.Claim;
import com.markstewart.model.ClaimState;
import io.helidon.common.mapper.OptionalValue;
import io.helidon.http.*;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static io.helidon.http.HeaderNames.SERVER;
import static io.helidon.http.HeaderValues.CONTENT_TYPE_JSON;
import static io.helidon.http.Status.*;

/**
 * An API providing basic Claims Management functionality.
 * NOT to be called by an end-user but rather by a web or service
 * tier.
 */
public class APIServer {

    final static Header OK_CONTENT_LEN_HEADER = HeaderValues.create(HeaderNames.CONTENT_LENGTH, 0);
    final static Header SERVER_HEADER = HeaderValues.createCached(SERVER, "AAA_LI_API");

    final static String HEALTH = "/health";
    final static String CREATE = "/create";
    final static String FIND   = "/find";
    final static String UPDATE = "/update";
    final static String DUPES  = "/dupes";

    private final DataServer dataServer = new DataServer(); // not threadsafe but see class comment.
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()); // threadsafe

    public APIServer(int port) {

        WebServer.builder()
                .port(port)
                .connectionConfig(config -> {
                            config.connectTimeout(Duration.of(60L, ChronoUnit.SECONDS));
                            config.keepAlive(true);
                        }
                )

                .routing(router -> {
                    router.get(HEALTH, new HealthCheckHandler());
                    //- Submit a new claim (no file upload needed, just metadata)
                    router.post(CREATE, new SubmitClaimHandler(dataServer, mapper));
                    // - Retrieve all claims for a given customer
                    router.get(FIND, new FindClaimsForCustomerHandler(dataServer, mapper));
                    //- Update a claim status (approve, deny)
                    router.put(UPDATE, new UpdateClaimHandler(dataServer));
                    //- Flag and retrieve potential duplicate claims
                    router.get(DUPES, new FindDuplicateClaimsHandler(dataServer, mapper));
                })

                .build()
                .start();
    }

    public static void main(String[] args) {
        APIServer server = new APIServer(8080);
    }

    private static class HealthCheckHandler implements Handler {
        @Override
        public void handle(ServerRequest req, ServerResponse res) throws Exception {
            res.header(OK_CONTENT_LEN_HEADER);
            res.header(CONTENT_TYPE_JSON);
            res.header(SERVER_HEADER);
            res.status(OK_200);
            res.send();
        }
    }

    private static class SubmitClaimHandler implements Handler {

        private final DataServer dataServer;
        private final ObjectMapper mapper;

        public SubmitClaimHandler(DataServer dataServer, ObjectMapper mapper) {
            this.dataServer = dataServer;
            this.mapper = mapper;
        }

        @Override
        public void handle(ServerRequest req, ServerResponse res) throws Exception {
            System.out.println("SubmitClaimHandler called");

            res.header(OK_CONTENT_LEN_HEADER);
            res.header(CONTENT_TYPE_JSON);
            res.header(SERVER_HEADER);

            String json = req.content().as(String.class);

            try {
                final Claim claim = mapper.readValue(json, Claim.class);
                boolean ok = dataServer.submitClaim(claim);
                res.status(OK_200);

            } catch (JsonProcessingException e) {
                System.out.println(e.getMessage());
                res.status(BAD_REQUEST_400);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                res.status(INTERNAL_SERVER_ERROR_500);
            }

            res.send();
        }
    }

    private static class FindClaimsForCustomerHandler implements Handler {

        private final DataServer dataServer;
        private final ObjectMapper mapper;

        public FindClaimsForCustomerHandler(DataServer dataServer, ObjectMapper mapper) {
            this.dataServer = dataServer;
            this.mapper = mapper;
        }

        @Override
        public void handle(ServerRequest req, ServerResponse res) throws Exception {
            System.out.println("FindClaimsForCustomerHandler called");

            res.header(CONTENT_TYPE_JSON);
            res.header(SERVER_HEADER);

            OptionalValue<String> customerId = req.query().first("id");
            if (customerId.isEmpty()) {
                System.out.println("No id parameter specified");
                res.status(BAD_REQUEST_400);
                res.send();
                return;
            }

            try {
                final List<Claim> customerClaims = dataServer.findClaimsByCustomer(UUID.fromString(customerId.get()));
                int claimsFound = customerClaims == null ? 0 : customerClaims.size();
                System.out.println("Found " + claimsFound + " claims for customer, " + customerId.get());
                final String claimsJson = mapper.writeValueAsString(customerClaims);
                System.out.println(claimsJson);
                res.status(OK_200);
                res.send(claimsJson.getBytes());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                res.status(INTERNAL_SERVER_ERROR_500);
                res.send();
            }

        }
    }

    /**
     * NOTE: I'm using query params with a PUT to pass the claim id and state here. Since the update
     * is only altering the state it doesn't seem necessary to send the entire resource (claim).
     * Not sure if Roy Fielding would approve but...
     */
    private static class UpdateClaimHandler implements Handler {

        private final DataServer dataServer;

        public UpdateClaimHandler(DataServer dataServer) {
            this.dataServer = dataServer;
        }

        @Override
        public void handle(ServerRequest req, ServerResponse res) throws Exception {
            System.out.println("UpdateClaimHandler called");

            res.header(CONTENT_TYPE_JSON);
            res.header(SERVER_HEADER);

            // get the required params from the query string
            OptionalValue<String> claimId = req.query().first("id");
            if (claimId.isEmpty()) {
                System.out.println("No claim id parameter specified");
                res.status(BAD_REQUEST_400);
                res.send();
                return;
            }

            OptionalValue<String> newState = req.query().first("state");
            if (newState.isEmpty()) {
                System.out.println("No state parameter specified");
                res.status(BAD_REQUEST_400);
                res.send();
                return;
            }

            ClaimState newClaimState;
            try {
                newClaimState = ClaimState.valueOf(newState.get().toUpperCase());
                UpdateStatus status = dataServer.updateClaimStatus(UUID.fromString(claimId.get()), newClaimState);
                switch (status) {
                    case CLAIM_NOT_FOUND -> res.status(NOT_FOUND_404);
                    case BAD_STATE_TRANSITION -> res.status(BAD_REQUEST_400);
                    case OK -> res.status(OK_200);
                }
            } catch (IllegalArgumentException e) { // bad state change param
                System.out.println("Unsupported state: " + newState.get());
                res.status(BAD_REQUEST_400);
            } finally {
                res.send();
            }

        }
    }

    private static class FindDuplicateClaimsHandler implements Handler {

        private final DataServer dataServer;
        private final ObjectMapper mapper;

        public FindDuplicateClaimsHandler(DataServer dataServer, ObjectMapper mapper) {
            this.dataServer = dataServer;
            this.mapper = mapper;
        }

        @Override
        public void handle(ServerRequest req, ServerResponse res) throws Exception {
            System.out.println("FindDuplicateClaimsHandler called");
            res.header(OK_CONTENT_LEN_HEADER);
            res.header(CONTENT_TYPE_JSON);
            res.header(SERVER_HEADER);



            res.status(OK_200);
            res.send();
        }
    }
}
