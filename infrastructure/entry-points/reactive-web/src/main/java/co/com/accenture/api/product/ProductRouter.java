package co.com.accenture.api.product;

import co.com.accenture.api.dto.NameRequest;
import co.com.accenture.api.dto.ProductRequest;
import co.com.accenture.api.dto.ProductResponse;
import co.com.accenture.api.dto.StockRequest;
import co.com.accenture.api.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ProductRouter {

    private static final String TAG = "Products";
    private static final String JSON = "application/json";

    @Bean
    @RouterOperations({
            @RouterOperation(path = "/api/branches/{branchId}/products", method = RequestMethod.POST,
                    beanClass = ProductHandler.class, beanMethod = "add",
                    operation = @Operation(operationId = "addProduct", summary = "Add a product to a branch", tags = TAG,
                            parameters = @Parameter(in = ParameterIn.PATH, name = "branchId", required = true, schema = @Schema(type = "string"),
                                    description = "Branch identifier"),
                            requestBody = @RequestBody(required = true,
                                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ProductRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Product created",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ProductResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid name, invalid stock or malformed request",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Branch not found",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "A product with this name already exists in the branch",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "500", description = "Unexpected error",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = "/api/branches/{branchId}/products/{productId}", method = RequestMethod.DELETE,
                    beanClass = ProductHandler.class, beanMethod = "delete",
                    operation = @Operation(operationId = "deleteProduct", summary = "Delete a product from a branch", tags = TAG,
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "branchId", required = true, schema = @Schema(type = "string"),
                                            description = "Branch identifier"),
                                    @Parameter(in = ParameterIn.PATH, name = "productId", required = true, schema = @Schema(type = "string"),
                                            description = "Product identifier")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "Product deleted", content = @Content),
                                    @ApiResponse(responseCode = "404", description = "Branch not found or product not in the branch",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "500", description = "Unexpected error",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = "/api/products/{productId}/stock", method = RequestMethod.PATCH,
                    beanClass = ProductHandler.class, beanMethod = "updateStock",
                    operation = @Operation(operationId = "updateProductStock", summary = "Update the stock of a product", tags = TAG,
                            parameters = @Parameter(in = ParameterIn.PATH, name = "productId", required = true, schema = @Schema(type = "string"),
                                    description = "Product identifier"),
                            requestBody = @RequestBody(required = true,
                                    content = @Content(mediaType = JSON, schema = @Schema(implementation = StockRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Stock updated",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ProductResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid stock or malformed request",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Product not found",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "500", description = "Unexpected error",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = "/api/products/{productId}/name", method = RequestMethod.PATCH,
                    beanClass = ProductHandler.class, beanMethod = "rename",
                    operation = @Operation(operationId = "renameProduct", summary = "Rename a product", tags = TAG,
                            parameters = @Parameter(in = ParameterIn.PATH, name = "productId", required = true, schema = @Schema(type = "string"),
                                    description = "Product identifier"),
                            requestBody = @RequestBody(required = true,
                                    content = @Content(mediaType = JSON, schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Product renamed",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ProductResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid name or malformed request",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Product not found",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "A product with this name already exists in the branch",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "500", description = "Unexpected error",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class)))
                            }))
    })
    public RouterFunction<ServerResponse> productRoutes(ProductHandler handler) {
        return route(POST("/api/branches/{branchId}/products"), handler::add)
                .andRoute(DELETE("/api/branches/{branchId}/products/{productId}"), handler::delete)
                .andRoute(PATCH("/api/products/{productId}/stock"), handler::updateStock)
                .andRoute(PATCH("/api/products/{productId}/name"), handler::rename);
    }
}
