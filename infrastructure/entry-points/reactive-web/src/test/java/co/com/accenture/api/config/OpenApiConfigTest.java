package co.com.accenture.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    void shouldDescribeApiTitleDescriptionAndVersion() {
        Info info = config.franchiseOpenApi().getInfo();

        assertThat(info.getTitle()).isEqualTo("Franchise API");
        assertThat(info.getDescription()).isNotBlank();
        assertThat(info.getVersion()).isEqualTo("1.0.0");
    }

    @Test
    void shouldAddPayloadAndMediaTypeErrorsOnlyToOperationsWithBody() {
        Operation withBody = new Operation().requestBody(new RequestBody())
                .responses(new ApiResponses().addApiResponse("201", new ApiResponse().description("Created")));
        Operation withoutBody = new Operation()
                .responses(new ApiResponses().addApiResponse("200", new ApiResponse().description("Ok")));
        OpenAPI openApi = new OpenAPI().paths(new Paths()
                .addPathItem("/with-body", new PathItem().post(withBody))
                .addPathItem("/without-body", new PathItem().get(withoutBody)));

        config.commonErrorResponses().customise(openApi);

        assertThat(withBody.getResponses()).containsOnlyKeys("201", "413", "415", "500");
        assertThat(withoutBody.getResponses()).containsOnlyKeys("200", "500");
        assertErrorResponse(withBody.getResponses().get("413"), "Request body exceeds the maximum allowed size");
        assertErrorResponse(withBody.getResponses().get("415"), "Content-Type is not application/json");
        assertErrorResponse(withoutBody.getResponses().get("500"), "Unexpected error");
    }

    private void assertErrorResponse(ApiResponse response, String description) {
        assertThat(response.getDescription()).isEqualTo(description);
        assertThat(response.getContent().get("application/json").getSchema().get$ref())
                .isEqualTo("#/components/schemas/ErrorResponse");
    }
}
