package co.com.accenture.blocking;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NonBlockingEndToEndTest {

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.0");

    @LocalServerPort
    private int port;

    private WebTestClient client;

    @BeforeEach
    void createClient() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Test
    void shouldDetectBlockingCallsOnNonBlockingThreads() {
        assertThatThrownBy(() -> Mono.fromRunnable(NonBlockingEndToEndTest::sleep)
                .subscribeOn(Schedulers.parallel())
                .block())
                .hasCauseInstanceOf(BlockingOperationError.class);
    }

    @Test
    void shouldServeEveryEndpointWithoutBlockingTheEventLoop() {
        String franchiseId = id(post("/api/franchises", Map.of("name", "Pizza Place"), 201));
        post("/api/franchises", Map.of("name", "Pizza Place"), 409);
        patch("/api/franchises/" + franchiseId + "/name", Map.of("name", "Pizza Palace"), 200);
        patch("/api/franchises/000000000000000000000000/name", Map.of("name", "Missing"), 404);

        String branchId = id(post("/api/franchises/" + franchiseId + "/branches", Map.of("name", "Airport"), 201));
        post("/api/franchises/" + franchiseId + "/branches", Map.of("name", "Airport"), 409);
        patch("/api/branches/" + branchId + "/name", Map.of("name", "Airport Terminal 1"), 200);

        String productId = id(post("/api/branches/" + branchId + "/products", Map.of("name", "Pepperoni", "stock", 15), 201));
        post("/api/branches/" + branchId + "/products", Map.of("name", "Calzone", "stock", 9), 201);
        post("/api/branches/" + branchId + "/products", Map.of("name", "Invalid", "stock", -1), 400);
        patch("/api/products/" + productId + "/stock", Map.of("stock", 35), 200);
        patch("/api/products/" + productId + "/name", Map.of("name", "Pepperoni Large"), 200);
        patch("/api/products/" + productId + "/name", Map.of("name", "Calzone"), 409);

        client.post().uri("/api/franchises").contentType(MediaType.APPLICATION_JSON).bodyValue("{\"name\":")
                .exchange().expectStatus().isBadRequest();
        client.post().uri("/api/franchises").contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"" + "x".repeat(300_000) + "\"}")
                .exchange().expectStatus().isEqualTo(413);

        client.get().uri("/api/franchises/" + franchiseId + "/top-stock-products").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].branchName").isEqualTo("Airport Terminal 1")
                .jsonPath("$[0].productName").isEqualTo("Pepperoni Large")
                .jsonPath("$[0].stock").isEqualTo(35);
        client.get().uri("/api/franchises/000000000000000000000000/top-stock-products").exchange()
                .expectStatus().isNotFound();

        client.delete().uri("/api/branches/" + branchId + "/products/" + productId).exchange()
                .expectStatus().isNoContent();
        client.delete().uri("/api/branches/" + branchId + "/products/" + productId).exchange()
                .expectStatus().isNotFound();

        client.get().uri("/actuator/health").exchange().expectStatus().isOk();
        client.get().uri("/actuator/prometheus").exchange().expectStatus().isNotFound();
        client.get().uri("/v3/api-docs").exchange().expectStatus().isOk();
        client.get().uri("/unknown").exchange().expectStatus().isNotFound();
    }

    private WebTestClient.BodyContentSpec post(String uri, Map<String, Object> body, int status) {
        return client.post().uri(uri).contentType(MediaType.APPLICATION_JSON).bodyValue(body)
                .exchange().expectStatus().isEqualTo(status).expectBody();
    }

    private WebTestClient.BodyContentSpec patch(String uri, Map<String, Object> body, int status) {
        return client.patch().uri(uri).contentType(MediaType.APPLICATION_JSON).bodyValue(body)
                .exchange().expectStatus().isEqualTo(status).expectBody();
    }

    private static String id(WebTestClient.BodyContentSpec body) {
        byte[] content = body.returnResult().getResponseBodyContent();
        return JsonPath.read(new String(content, StandardCharsets.UTF_8), "$.id");
    }

    private static void sleep() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
