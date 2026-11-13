package co.com.accenture.api.franchise;

import co.com.accenture.api.error.GlobalErrorHandler;
import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import co.com.accenture.model.franchise.Franchise;
import co.com.accenture.usecase.createfranchise.CreateFranchiseUseCase;
import co.com.accenture.usecase.renamefranchise.RenameFranchiseUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {FranchiseRouter.class, FranchiseHandler.class, GlobalErrorHandler.class})
class FranchiseRouterTest {

    private final Franchise franchise = Franchise.builder().id("f1").name("Burger House").build();

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private CreateFranchiseUseCase createFranchiseUseCase;

    @MockitoBean
    private RenameFranchiseUseCase renameFranchiseUseCase;

    @Test
    void shouldCreateFranchiseAndReturn201() {
        when(createFranchiseUseCase.execute("Burger House")).thenReturn(Mono.just(franchise));

        post("{\"name\":\"Burger House\"}")
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json("{\"id\":\"f1\",\"name\":\"Burger House\"}", true);
    }

    @Test
    void shouldPassMissingNameToUseCaseAndReturn400() {
        when(createFranchiseUseCase.execute(isNull()))
                .thenReturn(Mono.error(new BusinessException(BusinessError.INVALID_NAME)));

        post("{}")
                .expectStatus().isBadRequest()
                .expectBody().json("{\"code\":\"INVALID_NAME\",\"message\":\"Name must not be empty\"}", true);
    }

    @Test
    void shouldReturn409WhenFranchiseNameExists() {
        when(createFranchiseUseCase.execute("Burger House"))
                .thenReturn(Mono.error(new BusinessException(BusinessError.FRANCHISE_NAME_ALREADY_EXISTS)));

        post("{\"name\":\"Burger House\"}")
                .expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.code").isEqualTo("FRANCHISE_NAME_ALREADY_EXISTS");
    }

    @Test
    void shouldReturn400ForMalformedJsonWithoutCallingUseCase() {
        post("{\"name\":")
                .expectStatus().isBadRequest()
                .expectBody().json("{\"code\":\"INVALID_REQUEST\",\"message\":\"Request body is missing or malformed\"}", true);

        verify(createFranchiseUseCase, never()).execute(any());
    }

    @Test
    void shouldReturn400ForEmptyBodyWithoutCallingUseCase() {
        client.post().uri("/api/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("INVALID_REQUEST");

        verify(createFranchiseUseCase, never()).execute(any());
    }

    @Test
    void shouldReturn500WithGenericMessageOnUnexpectedError() {
        when(createFranchiseUseCase.execute("Burger House"))
                .thenReturn(Mono.error(new IllegalStateException("mongo timeout at 10.0.0.1")));

        post("{\"name\":\"Burger House\"}")
                .expectStatus().is5xxServerError()
                .expectBody().json("{\"code\":\"INTERNAL_ERROR\",\"message\":\"An unexpected error occurred\"}", true);
    }

    @Test
    void shouldRenameFranchiseAndReturn200() {
        Franchise renamed = Franchise.builder().id("f1").name("Pizza Place").build();
        when(renameFranchiseUseCase.execute("f1", "Pizza Place")).thenReturn(Mono.just(renamed));

        patch("{\"name\":\"Pizza Place\"}")
                .expectStatus().isOk()
                .expectBody().json("{\"id\":\"f1\",\"name\":\"Pizza Place\"}", true);
    }

    @Test
    void shouldReturn404WhenRenamingMissingFranchise() {
        when(renameFranchiseUseCase.execute("f1", "Pizza Place"))
                .thenReturn(Mono.error(new BusinessException(BusinessError.FRANCHISE_NOT_FOUND)));

        patch("{\"name\":\"Pizza Place\"}")
                .expectStatus().isNotFound()
                .expectBody().json("{\"code\":\"FRANCHISE_NOT_FOUND\",\"message\":\"Franchise not found\"}", true);
    }

    @Test
    void shouldReturn400WhenRenamingWithBlankName() {
        when(renameFranchiseUseCase.execute("f1", " "))
                .thenReturn(Mono.error(new BusinessException(BusinessError.INVALID_NAME)));

        patch("{\"name\":\" \"}")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("INVALID_NAME");
    }

    @Test
    void shouldReturn409WhenRenamingToExistingName() {
        when(renameFranchiseUseCase.execute("f1", "Taken"))
                .thenReturn(Mono.error(new BusinessException(BusinessError.FRANCHISE_NAME_ALREADY_EXISTS)));

        patch("{\"name\":\"Taken\"}")
                .expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.code").isEqualTo("FRANCHISE_NAME_ALREADY_EXISTS");
    }

    @Test
    void shouldReturn400ForMalformedJsonOnRenameWithoutCallingUseCase() {
        patch("not json")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("INVALID_REQUEST");

        verify(renameFranchiseUseCase, never()).execute(anyString(), any());
    }

    private WebTestClient.ResponseSpec post(String body) {
        return client.post().uri("/api/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange();
    }

    private WebTestClient.ResponseSpec patch(String body) {
        return client.patch().uri("/api/franchises/f1/name")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange();
    }
}
