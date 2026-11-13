package co.com.accenture.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI franchiseOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Franchise API")
                .description("Reactive REST API to manage franchises, their branches and the stock of their products.")
                .version("1.0.0"));
    }
}
