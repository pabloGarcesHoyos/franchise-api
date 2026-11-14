package co.com.accenture.api.error;

import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalErrorHandlerTest {

    private final GlobalErrorHandler handler = new GlobalErrorHandler(ServerCodecConfigurer.create());

    static Stream<Arguments> businessErrors() {
        return Stream.of(
                Arguments.of(BusinessError.INVALID_NAME, HttpStatus.BAD_REQUEST),
                Arguments.of(BusinessError.INVALID_STOCK, HttpStatus.BAD_REQUEST),
                Arguments.of(BusinessError.FRANCHISE_NOT_FOUND, HttpStatus.NOT_FOUND),
                Arguments.of(BusinessError.BRANCH_NOT_FOUND, HttpStatus.NOT_FOUND),
                Arguments.of(BusinessError.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND),
                Arguments.of(BusinessError.FRANCHISE_NAME_ALREADY_EXISTS, HttpStatus.CONFLICT),
                Arguments.of(BusinessError.BRANCH_NAME_ALREADY_EXISTS, HttpStatus.CONFLICT),
                Arguments.of(BusinessError.PRODUCT_NAME_ALREADY_EXISTS, HttpStatus.CONFLICT));
    }

    @ParameterizedTest
    @MethodSource("businessErrors")
    void shouldMapBusinessErrorTypeToStatusWithCodeAndMessage(BusinessError error, HttpStatus status) {
        MockServerWebExchange exchange = exchange();

        StepVerifier.create(handler.handle(exchange, new BusinessException(error)))
                .verifyComplete();

        assertResponse(exchange, status,
                "{\"code\":\"" + error.getCode() + "\",\"message\":\"" + error.getMessage() + "\"}");
    }

    @Test
    void shouldMapUnreadableInputToInvalidRequest() {
        MockServerWebExchange exchange = exchange();

        StepVerifier.create(handler.handle(exchange, new ServerWebInputException("Failed to read HTTP message")))
                .verifyComplete();

        assertResponse(exchange, HttpStatus.BAD_REQUEST,
                "{\"code\":\"INVALID_REQUEST\",\"message\":\"Request body is missing or malformed\"}");
    }

    @Test
    void shouldKeepStatusOfFrameworkErrorsWithoutExposingReason() {
        MockServerWebExchange exchange = exchange();

        StepVerifier.create(handler.handle(exchange,
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "No route for /internal/details")))
                .verifyComplete();

        assertResponse(exchange, HttpStatus.NOT_FOUND, "{\"code\":\"NOT_FOUND\",\"message\":\"Not Found\"}");
    }

    @Test
    void shouldMapMethodNotAllowed() {
        MockServerWebExchange exchange = exchange();

        StepVerifier.create(handler.handle(exchange, new MethodNotAllowedException("PUT", List.of())))
                .verifyComplete();

        assertResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED,
                "{\"code\":\"METHOD_NOT_ALLOWED\",\"message\":\"Method Not Allowed\"}");
    }

    @Test
    void shouldHideUnexpectedErrorDetails() {
        MockServerWebExchange exchange = exchange();

        StepVerifier.create(handler.handle(exchange, new IllegalStateException("connection string leaked")))
                .verifyComplete();

        assertResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR,
                "{\"code\":\"INTERNAL_ERROR\",\"message\":\"An unexpected error occurred\"}");
    }

    private MockServerWebExchange exchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.post("/api/franchises"));
    }

    private void assertResponse(MockServerWebExchange exchange, HttpStatus status, String body) {
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(status);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        StepVerifier.create(exchange.getResponse().getBodyAsString())
                .expectNext(body)
                .verifyComplete();
    }
}
