package co.com.accenture.api.branch;

import co.com.accenture.api.RequestBodies;
import co.com.accenture.api.dto.BranchResponse;
import co.com.accenture.api.dto.NameRequest;
import co.com.accenture.usecase.addbranch.AddBranchUseCase;
import co.com.accenture.usecase.renamebranch.RenameBranchUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BranchHandler {

    private final AddBranchUseCase addBranchUseCase;
    private final RenameBranchUseCase renameBranchUseCase;

    public Mono<ServerResponse> add(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return RequestBodies.required(request, NameRequest.class)
                .flatMap(body -> addBranchUseCase.execute(franchiseId, body.name()))
                .map(BranchResponse::from)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> rename(ServerRequest request) {
        String branchId = request.pathVariable("branchId");
        return RequestBodies.required(request, NameRequest.class)
                .flatMap(body -> renameBranchUseCase.execute(branchId, body.name()))
                .map(BranchResponse::from)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }
}
