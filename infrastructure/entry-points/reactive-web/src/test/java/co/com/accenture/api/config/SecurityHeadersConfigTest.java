package co.com.accenture.api.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityHeadersConfigTest {

    private final SecurityHeadersConfig filter = new SecurityHeadersConfig();

    @Test
    void shouldAddSecurityHeadersAndContinueChain() {
        MockServerWebExchange exchange = exchange("/api/franchises");
        exchange.getResponse().getHeaders().set("Server", "netty");

        HttpHeaders headers = filter(exchange);

        assertThat(headers.getFirst("Content-Security-Policy"))
                .isEqualTo("default-src 'self'; frame-ancestors 'self'; form-action 'self'");
        assertThat(headers.getFirst("Strict-Transport-Security"))
                .isEqualTo("max-age=31536000; includeSubDomains; preload");
        assertThat(headers.getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(headers.getFirst("Cache-Control")).isEqualTo("no-store");
        assertThat(headers.getFirst("Pragma")).isEqualTo("no-cache");
        assertThat(headers.getFirst("Referrer-Policy")).isEqualTo("strict-origin-when-cross-origin");
        assertThat(headers.containsHeader("Server")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/swagger-ui.html", "/swagger-ui/index.html", "/swagger-ui/swagger-ui.css",
            "/v3/api-docs", "/v3/api-docs/swagger-config"})
    void shouldRelaxContentSecurityPolicyOnlyForDocumentation(String path) {
        HttpHeaders headers = filter(exchange(path));

        assertThat(headers.getFirst("Content-Security-Policy"))
                .isEqualTo("default-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; "
                        + "frame-ancestors 'self'; form-action 'self'");
        assertThat(headers.getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/franchises/f1/top-stock-products", "/swagger-uifake", "/v3/api-docsx",
            "/api/swagger-ui/index.html", "/actuator/health"})
    void shouldKeepStrictContentSecurityPolicyElsewhere(String path) {
        HttpHeaders headers = filter(exchange(path));

        assertThat(headers.getFirst("Content-Security-Policy"))
                .isEqualTo("default-src 'self'; frame-ancestors 'self'; form-action 'self'");
    }

    private MockServerWebExchange exchange(String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path));
    }

    private HttpHeaders filter(MockServerWebExchange exchange) {
        WebFilterChain chain = filteredExchange -> Mono.empty();
        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
        return exchange.getResponse().getHeaders();
    }
}
