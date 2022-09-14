package simulations;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.Owner;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpDsl;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import jodd.util.RandomString;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import static utils.Config.*;

public class OwnersSimulation extends Simulation {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String OWNERS_ENDPOINT = "/petclinic/api/owners";

    private Iterator<Map<String, Object>> firstNameFeeder =
            Stream.generate((Supplier<Map<String, Object>>) ()
                    -> Collections.singletonMap("firstName", RandomString.get().randomAlpha(10))
            ).iterator();


    //Define http protocol for all requests
    HttpProtocolBuilder httpProtocol = HttpDsl.http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .basicAuth(USERNAME, PASSWORD)
            .userAgentHeader("Gatling/Performance Test");


    ChainBuilder getOwnersReq = CoreDsl.exec(http("Get owners")
            .get(OWNERS_ENDPOINT));


    ChainBuilder createOwnerReq = feed(csv("data/owners.csv").random())
            .exec(http("Create owner")
                    .post(OWNERS_ENDPOINT)
                    .body(StringBody(GSON.toJson(Owner.builder()
                            .firstName("#{firstName}")
                            .lastName("#{lastName}")
                            .telephone("#{telephone}")
                            .city("#{city}")
                            .address("#{address}")
                            .build())))
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("ownerId")));

    ChainBuilder updateOwnerReq = feed(firstNameFeeder)
            .exec(http("Update owner")
            .put(OWNERS_ENDPOINT + "/#{ownerId}")
            .body(StringBody(GSON.toJson(Owner.builder()
                    .firstName("#{firstName}")
                    .lastName("Updated")
                    .telephone("1231234123")
                    .city("Sofia")
                    .address("Stranski street")
                    .build())))
            .check(status().is(204)));

    ChainBuilder getLatestOwnerReq = CoreDsl.exec(http("Get latest owner")
            .get(OWNERS_ENDPOINT + "/#{ownerId}")
            .check(status().is(200))
            .check(responseTimeInMillis().lt(100)));

    ChainBuilder deleteOwnerReq = CoreDsl.exec(http("Delete latest owner")
            .delete(OWNERS_ENDPOINT + "/#{ownerId}"));

    ScenarioBuilder getOwnersScn = CoreDsl.scenario("Get owners")
            .exec(getOwnersReq);

    ScenarioBuilder createOwnerScenario = CoreDsl.scenario("Create owner")
            .exec(createOwnerReq, updateOwnerReq, getLatestOwnerReq, deleteOwnerReq);


   {
        setUp(createOwnerScenario.injectOpen(constantUsersPerSec(USERS).during(DURATION)),
                        getOwnersScn.injectOpen(constantUsersPerSec(USERS).during(DURATION)))
                .assertions(global().responseTime().max().lt(500),
                        global().successfulRequests().percent().gt(95.0))
                .protocols(httpProtocol);
    }


}
