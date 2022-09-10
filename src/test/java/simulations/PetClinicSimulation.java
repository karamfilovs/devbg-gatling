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

import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class PetClinicSimulation extends Simulation {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    HttpProtocolBuilder httpProtocol = HttpDsl.http
            .baseUrl("http://localhost:9966")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .basicAuth("admin", "admin")
            .userAgentHeader("Gatling/Performance Test");

    Iterator<Map<String, Object>> feeder =
            Stream.generate((Supplier<Map<String, Object>>) ()
                    -> Collections.singletonMap("username", UUID.randomUUID().toString())
            ).iterator();

    ChainBuilder getOwnersReq = CoreDsl.exec(http("Get owners")
            .get("/petclinic/api/owners"));


    ChainBuilder createOwnerReq = CoreDsl.exec(http("Create owner")
            .post("/petclinic/api/owners").
            body(StringBody(GSON.toJson(Owner.builder()
                    .firstName("Alex")
                    .lastName("Karamfilov")
                    .telephone("1231234123")
                    .city("Sofia")
                    .address("Stranski street")
                    .build())))
            .check(status().is(201))
            .check(jsonPath("$.id").saveAs("ownerId")));

    ChainBuilder updateOwnerReq = CoreDsl.exec(http("Update owner")
            .put("/petclinic/api/owners/#{ownerId}").
            body(StringBody(GSON.toJson(Owner.builder()
                    .firstName("Alex")
                    .lastName("Updated")
                    .telephone("1231234123")
                    .city("Sofia")
                    .address("Stranski street")
                    .build())))
            .check(status().is(204)));

    ChainBuilder getLatestOwnerReq = CoreDsl.exec(http("Get latest owner")
            .get("/petclinic/api/owners/#{ownerId}")
            .check(status().is(200)));

    ChainBuilder deleteOwnerReq = CoreDsl.exec(http("Delete latest owner")
            .delete("/petclinic/api/owners/#{ownerId}"));

    ScenarioBuilder getOwnersScn = CoreDsl.scenario("Get owners")
            .exec(getOwnersReq);

    ScenarioBuilder createOwnerScenario = CoreDsl.scenario("Create owner")
            .exec(createOwnerReq, updateOwnerReq, getLatestOwnerReq, deleteOwnerReq);

    ScenarioBuilder scn = CoreDsl.scenario("Load Test Creating Customers")
            .feed(feeder)
            .exec(http("create-customer-request")
                    .post("/api/customers")
                    .header("Content-Type", "application/json")
                    .body(StringBody("{ \"username\": \"#{username}\" }"))
                    .check(status().is(201))
                    .check(header("Location").saveAs("location"))
            )
            .exec(http("get-customer-request")
                    .get(session -> session.getString("location"))
                    .check(status().is(200))
            );

    public PetClinicSimulation() {
        this.setUp(createOwnerScenario.injectOpen(constantUsersPerSec(3).during(Duration.ofSeconds(1))),
                        getOwnersScn.injectOpen(constantUsersPerSec(1).during(Duration.ofSeconds(1))))
                .protocols(httpProtocol);
    }


}
