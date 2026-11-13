package co.com.accenture.api.branch;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class BranchRouter {

    @Bean
    public RouterFunction<ServerResponse> branchRoutes(BranchHandler handler) {
        return route(POST("/api/franchises/{franchiseId}/branches"), handler::add)
                .andRoute(PATCH("/api/branches/{branchId}/name"), handler::rename);
    }
}
