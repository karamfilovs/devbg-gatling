package simulations;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.Owner;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.HttpDsl;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import jodd.util.RandomString;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class PetClinicSimulation extends Simulation {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String OWNERS_ENDPOINT = "/petclinic/api/owners";
    private static final String VETS_ENDPOINT = "/petclinic/api/vets";
    private  Iterator<Map<String, Object>> firstNameFeeder =
            Stream.generate((Supplier<Map<String, Object>>) ()
                    -> Collections.singletonMap("firstName", RandomString.get().randomAlpha(10))
            ).iterator();

    HttpProtocolBuilder httpProtocol = HttpDsl.http
            .baseUrl("http://localhost:9966")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .basicAuth("admin", "admin")
            .userAgentHeader("Gatling/Performance Test");



    ChainBuilder getOwnersReq = CoreDsl.exec(http("Get owners")
            .get(OWNERS_ENDPOINT));


    ChainBuilder createOwnerReq = CoreDsl.feed(csv("data/owners.csv").circular())
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

    ChainBuilder updateOwnerReq = CoreDsl.exec(http("Update owner")
            .put(OWNERS_ENDPOINT + "/#{ownerId}").
            body(StringBody(GSON.toJson(Owner.builder()
                    .firstName("Alex")
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


    public PetClinicSimulation() {
        this.setUp(createOwnerScenario.injectOpen(constantUsersPerSec(3).during(Duration.ofSeconds(1))),
                        getOwnersScn.injectOpen(constantUsersPerSec(1).during(Duration.ofSeconds(1))))
                .protocols(httpProtocol);
    }


}
