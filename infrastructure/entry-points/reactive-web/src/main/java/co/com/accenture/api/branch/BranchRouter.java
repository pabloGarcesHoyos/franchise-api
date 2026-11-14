package co.com.accenture.api.branch;

import co.com.accenture.api.dto.BranchResponse;
import co.com.accenture.api.dto.NameRequest;
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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class BranchRouter {

    private static final String FRANCHISE_BRANCHES = "/api/franchises/{franchiseId}/branches";
    private static final String BRANCH_NAME = "/api/branches/{branchId}/name";

    private static final String TAG = "Branches";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Bean
    @RouterOperations({
            @RouterOperation(path = FRANCHISE_BRANCHES, method = RequestMethod.POST,
                    beanClass = BranchHandler.class, beanMethod = "add",
                    operation = @Operation(operationId = "addBranch", summary = "Add a branch to a franchise",
                            description = "Creates a branch in an existing franchise. The name is trimmed and must be unique within the franchise.",
                            tags = TAG,
                            parameters = @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true, schema = @Schema(type = "string"),
                                    description = "Franchise identifier"),
                            requestBody = @RequestBody(required = true,
                                    content = @Content(mediaType = JSON, schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Branch created",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = BranchResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid name or malformed request",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "A branch with this name already exists in the franchise",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = BRANCH_NAME, method = RequestMethod.PATCH,
                    beanClass = BranchHandler.class, beanMethod = "rename",
                    operation = @Operation(operationId = "renameBranch", summary = "Rename a branch",
                            description = "Changes the name of an existing branch and returns the updated branch.",
                            tags = TAG,
                            parameters = @Parameter(in = ParameterIn.PATH, name = "branchId", required = true, schema = @Schema(type = "string"),
                                    description = "Branch identifier"),
                            requestBody = @RequestBody(required = true,
                                    content = @Content(mediaType = JSON, schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Branch renamed",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = BranchResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid name or malformed request",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Branch not found",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "A branch with this name already exists in the franchise",
                                            content = @Content(mediaType = JSON, schema = @Schema(implementation = ErrorResponse.class)))
                            }))
    })
    public RouterFunction<ServerResponse> branchRoutes(BranchHandler handler) {
        return route(POST(FRANCHISE_BRANCHES), handler::add)
                .andRoute(PATCH(BRANCH_NAME), handler::rename);
    }
}
