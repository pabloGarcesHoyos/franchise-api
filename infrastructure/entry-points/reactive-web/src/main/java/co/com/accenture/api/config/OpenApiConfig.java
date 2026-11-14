package co.com.accenture.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    static final String ERROR_SCHEMA = "#/components/schemas/ErrorResponse";

    @Bean
    public OpenAPI franchiseOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Franchise API")
                .description("Reactive REST API to manage franchises, their branches and the stock of their products.")
                .version("1.0.0"));
    }

    @Bean
    public OpenApiCustomizer commonErrorResponses() {
        return openApi -> openApi.getPaths().values().forEach(path -> path.readOperations().forEach(operation -> {
            if (operation.getRequestBody() != null) {
                operation.getResponses()
                        .addApiResponse("413", errorResponse("Request body exceeds the maximum allowed size"))
                        .addApiResponse("415", errorResponse("Content-Type is not application/json"));
            }
            operation.getResponses().addApiResponse("500", errorResponse("Unexpected error"));
        }));
    }

    private static ApiResponse errorResponse(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType().schema(new Schema<>().$ref(ERROR_SCHEMA))));
    }
}
