package co.com.accenture.api.product;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ProductRouter {

    @Bean
    public RouterFunction<ServerResponse> productRoutes(ProductHandler handler) {
        return route(POST("/api/branches/{branchId}/products"), handler::add)
                .andRoute(DELETE("/api/branches/{branchId}/products/{productId}"), handler::delete)
                .andRoute(PATCH("/api/products/{productId}/stock"), handler::updateStock)
                .andRoute(PATCH("/api/products/{productId}/name"), handler::rename);
    }
}
