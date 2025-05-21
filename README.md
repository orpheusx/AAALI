PART 2

These are some of my working notes fwiw. Below are some useful curl commands for testing the APIServer.

Insurance claim data
- Policy Number
- Deceased's full name and address, DOB, optionally SS #
- Death certificate (represented by the state and certificate number.)
- Same info for the primary beneficiary plus contact info: phone, email address
- Payment information? Just bank routing number to simplify.
- current status used by workflow (related to but distinct from audit trail activities):
    PENDING_REVIEW -> IN_REVIEW -> COMPLETE


Note: I haven't implemented an audit trail but the ClaimState enum would be useful if I did.

Note: To improve it's consumability by clients, a good addition to this project would be an OpenAPI (née Swagger) spec. Writing one manually is pretty time consuming, though, so I deferred it while looking around for some support tooling. 

Note: I'm using Records for the data models. One downside of this choice is that most of the JPA implementations I'm aware of only support mutable Java Beans (aka POJOs.) For the purposes of the API, the SQL for persisting state changes shouldn't be terribly complex.

API:

To test the API:
- curl -X POST --verbose http://localhost:8080/create -d '...' -H 'Content-Type: application/json'
- curl -X GET --verbose http://localhost:8080/find?id=...  -H 'Content-Type: application/json'
- curl -X PUT --verbose http://localhost:8080/update -d '...' -H 'Content-Type: application/json'
- curl -X GET --verbose http://localhost:8080/dupes?id=...  -H 'Content-Type: application/json'

Examples:
```
# Enter a claim with id 7213f19f-e28d-40a8-856c-c8478bedde61
curl --verbose -X POST http://localhost:8080/create -d '{ "submittedAt" : 1747790875.250820000, "id" : "7213f19f-e28d-40a8-856c-c8478bedde61", "policyId" : "09876543456-4432", "deceased" : { "customer" : { "id" : "f2d8c645-889f-4a98-82eb-d46527c52452", "surname" : "Stewart", "givenName" : "Mark", "streetAddress" : "22 Baker Street, Boston", "stateOrProvince" : "MA", "postalCode" : "55555", "emailAddress" : "bogusemailaddress@gmail.com", "dob" : [ 1921, 12, 23 ], "socialInsuranceId" : "123456789" } }, "policyHolder" : { "customer" : { "id" : "f2d8c645-889f-4a98-82eb-d46527c52452", "surname" : "Stewart", "givenName" : "Mark", "streetAddress" : "22 Baker Street, Boston", "stateOrProvince" : "MA", "postalCode" : "55555", "emailAddress" : "bogusemailaddress@gmail.com", "dob" : [ 1921, 12, 23 ], "socialInsuranceId" : "123456789" } }, "state" : "RECEIVED" }' -H 'Content-Type: application/json'

# Attempt to update the claim status illegally (RECEIVED -> DENIED is not an allowed transition)
curl --verbose -X PUT "http://localhost:8080/update?id=7213f19f-e28d-40a8-856c-c8478bedde61&state=denied" -H 'Content-Type: application/json'

# Attempt to update the claim status legally (RECEIVED -> IN_REVIEW is an OK transition)
curl --verbose -X PUT "http://localhost:8080/update?id=7213f19f-e28d-40a8-856c-c8478bedde61&state=in_review" -H 'Content-Type: application/json'

# Fetch all the Claims for a given customer (f2d8c645-889f-4a98-82eb-d46527c52452) returning an array of json.
curl --verbose -X GET "http://localhost:8080/find?id=f2d8c645-889f-4a98-82eb-d46527c52452" -H 'Content-Type: application/json'

# Add a duplicate (for the same policy id) claim
curl -X POST --verbose http://localhost:8080/create -d '{ "submittedAt" : 1747790875.250820000, "id" : "a752f155-54f4-4961-b12c-6f164f79252a", "policyId" : "09876543456-4432", "deceased" : { "customer" : { "id" : "f2d8c645-889f-4a98-82eb-d46527c52452", "surname" : "Stewart", "givenName" : "Mark", "streetAddress" : "22 Baker Street, Boston", "stateOrProvince" : "MA", "postalCode" : "55555", "emailAddress" : "bogusemailaddress@gmail.com", "dob" : [ 1921, 12, 23 ], "socialInsuranceId" : "123456789" } }, "policyHolder" : { "customer" : { "id" : "f2d8c645-889f-4a98-82eb-d46527c52452", "surname" : "Stewart", "givenName" : "Mark", "streetAddress" : "22 Baker Street, Boston", "stateOrProvince" : "MA", "postalCode" : "55555", "emailAddress" : "bogusemailaddress@gmail.com", "dob" : [ 1921, 12, 23 ], "socialInsuranceId" : "123456789" } }, "state" : "RECEIVED" }' -H 'Content-Type: application/json'

# Fetch all duplicate claims made against policy id (09876543456-4432) returning an array of claims with the same policy id.
curl --verbose -X GET "http://localhost:8080/dupes?policyId=09876543456-4432" -H 'Content-Type: application/json'
```


**BUILD INSTRUCTIONS:**

With Maven 3.9.9+ installed

To run just tests:
```mvn test```

To run test and, if they pass, build an executable jar file:
```mvn package```

To run just the executable jar after building it:
```
java -jar ./target/AAALIClaimsMgmtAPI-1.0-SNAPSHOT-jar-with-dependencies.jar
```
With Docker 27.3.1+ installed, build an image:
```docker build -t aaali:0.1.0 -f Dockerfile .```

To run the resulting image:
```docker run -it --rm --name aaali -p 8080:8080 aaali:0.1.0```
