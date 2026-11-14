package co.com.accenture.api.franchise;

import co.com.accenture.api.dto.FranchiseResponse;
import co.com.accenture.api.dto.NameRequest;
import co.com.accenture.api.dto.TopStockProductResponse;
import co.com.accenture.api.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class FranchiseRouter {

    private static final String FRANCHISES = "/api/franchises";
    private static final String FRANCHISE_NAME = "/api/franchises/{franchiseId}/name";
    private static final String TOP_STOCK_PRODUCTS = "/api/franchises/{franchiseId}/top-stock-products";

    private static final String TAG = "Franchises";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Bean
    @RouterOperations({
            @RouterOperation(path = FRANCHISES, method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class, beanMethod = "create",
                    operation = @Operation(operationId = "createFranchise", summary = "Create a franchise",
                            description = "Creates a franchise. The name is trimmed and must be unique across all franchises.",
                            tags = TAG,
                            requestBody = @RequestBody(required = true,
                                    content = @Content(mediaType = JSON, schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Franchise created",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid name or malformed request",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "A franchise with this name already exists",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = FRANCHISE_NAME, method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class, beanMethod = "rename",
                    operation = @Operation(operationId = "renameFranchise", summary = "Rename a franchise",
                            description = "Changes the name of an existing franchise and returns the updated franchise.",
                            tags = TAG,
                            parameters = @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true, schema = @Schema(type = "string"),
                                    description = "Franchise identifier"),
                            requestBody = @RequestBody(required = true,
                                    content = @Content(mediaType = JSON, schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Franchise renamed",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid name or malformed request",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "A franchise with this name already exists",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = TOP_STOCK_PRODUCTS, method = RequestMethod.GET,
                    beanClass = FranchiseHandler.class, beanMethod = "topStockProducts",
                    operation = @Operation(operationId = "getTopStockProducts",
                            summary = "Get the product with the most stock in each branch of a franchise",
                            description = "Returns one product per branch, indicating the branch it belongs to. "
                                    + "Branches are ordered by name, branches without products are omitted "
                                    + "and ties are resolved by product name.",
                            tags = TAG,
                            parameters = @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true, schema = @Schema(type = "string"),
                                    description = "Franchise identifier"),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Top stock product per branch",
                                            content = @Content(mediaType = JSON, array = @ArraySchema(
                                                    schema = @Schema(implementation = TopStockProductResponse.class)))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class)))
                            }))
    })
    public RouterFunction<ServerResponse> franchiseRoutes(FranchiseHandler handler) {
        return route(POST(FRANCHISES), handler::create)
                .andRoute(PATCH(FRANCHISE_NAME), handler::rename)
                .andRoute(GET(TOP_STOCK_PRODUCTS), handler::topStockProducts);
    }
}
