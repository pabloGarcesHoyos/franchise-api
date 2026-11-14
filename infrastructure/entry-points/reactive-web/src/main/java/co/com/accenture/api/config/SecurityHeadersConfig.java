package co.com.accenture.api.config;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class SecurityHeadersConfig implements WebFilter {

    private static final String API_POLICY = "default-src 'self'; frame-ancestors 'self'; form-action 'self'";
    private static final String DOCUMENTATION_POLICY = "default-src 'self'; style-src 'self' 'unsafe-inline'; "
            + "img-src 'self' data:; frame-ancestors 'self'; form-action 'self'";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HttpHeaders headers = exchange.getResponse().getHeaders();
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        headers.set("Content-Security-Policy", isDocumentation(path) ? DOCUMENTATION_POLICY : API_POLICY);
        headers.set("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
        headers.set("X-Content-Type-Options", "nosniff");
        headers.remove("Server");
        headers.set("Cache-Control", "no-store");
        headers.set("Pragma", "no-cache");
        headers.set("Referrer-Policy", "strict-origin-when-cross-origin");
        return chain.filter(exchange);
    }

    private static boolean isDocumentation(String path) {
        return path.equals("/swagger-ui.html")
                || path.startsWith("/swagger-ui/")
                || path.equals("/v3/api-docs")
                || path.startsWith("/v3/api-docs/");
    }
}
