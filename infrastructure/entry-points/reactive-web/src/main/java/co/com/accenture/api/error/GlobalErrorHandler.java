package co.com.accenture.api.error;

import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@Order(-2)
@RequiredArgsConstructor
public class GlobalErrorHandler implements WebExceptionHandler {

    static final String INVALID_REQUEST = "INVALID_REQUEST";
    static final String INVALID_REQUEST_MESSAGE = "Request body is missing or malformed";
    static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    static final String INTERNAL_ERROR_MESSAGE = "An unexpected error occurred";

    private final ServerCodecConfigurer codecConfigurer;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable error) {
        return switch (error) {
            case BusinessException businessException -> write(exchange, statusOf(businessException.getError()),
                    new ErrorResponse(businessException.getError().getCode(), businessException.getMessage()));
            case ServerWebInputException ignored -> write(exchange, HttpStatus.BAD_REQUEST,
                    new ErrorResponse(INVALID_REQUEST, INVALID_REQUEST_MESSAGE));
            case ResponseStatusException statusException -> writeStatus(exchange, statusException.getStatusCode());
            default -> unexpected(exchange, error);
        };
    }

    private Mono<Void> writeStatus(ServerWebExchange exchange, HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        return write(exchange, status, new ErrorResponse(status.name(), status.getReasonPhrase()));
    }

    private Mono<Void> unexpected(ServerWebExchange exchange, Throwable error) {
        log.error("Unexpected error processing {} {}", exchange.getRequest().getMethod(),
                exchange.getRequest().getPath(), error);
        return write(exchange, HttpStatus.INTERNAL_SERVER_ERROR,
                new ErrorResponse(INTERNAL_ERROR, INTERNAL_ERROR_MESSAGE));
    }

    private Mono<Void> write(ServerWebExchange exchange, HttpStatus status, ErrorResponse body) {
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .flatMap(response -> response.writeTo(exchange, new ResponseContext(codecConfigurer.getWriters())));
    }

    private static HttpStatus statusOf(BusinessError error) {
        return switch (error.getType()) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
        };
    }

    private record ResponseContext(List<HttpMessageWriter<?>> messageWriters) implements ServerResponse.Context {

        @Override
        public List<ViewResolver> viewResolvers() {
            return List.of();
        }
    }
}
