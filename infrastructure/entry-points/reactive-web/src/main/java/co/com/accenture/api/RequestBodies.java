package co.com.accenture.api;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

public final class RequestBodies {

    private RequestBodies() {
    }

    public static <T> Mono<T> required(ServerRequest request, Class<T> type) {
        return request.bodyToMono(type)
                .switchIfEmpty(Mono.error(() -> new ServerWebInputException("Request body is required")));
    }
}
