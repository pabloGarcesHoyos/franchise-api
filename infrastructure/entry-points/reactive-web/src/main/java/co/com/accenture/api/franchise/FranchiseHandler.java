package co.com.accenture.api.franchise;

import co.com.accenture.api.RequestBodies;
import co.com.accenture.api.dto.FranchiseResponse;
import co.com.accenture.api.dto.NameRequest;
import co.com.accenture.usecase.createfranchise.CreateFranchiseUseCase;
import co.com.accenture.usecase.renamefranchise.RenameFranchiseUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class FranchiseHandler {

    private final CreateFranchiseUseCase createFranchiseUseCase;
    private final RenameFranchiseUseCase renameFranchiseUseCase;

    public Mono<ServerResponse> create(ServerRequest request) {
        return RequestBodies.required(request, NameRequest.class)
                .flatMap(body -> createFranchiseUseCase.execute(body.name()))
                .map(FranchiseResponse::from)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> rename(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return RequestBodies.required(request, NameRequest.class)
                .flatMap(body -> renameFranchiseUseCase.execute(franchiseId, body.name()))
                .map(FranchiseResponse::from)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }
}
