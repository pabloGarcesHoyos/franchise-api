package co.com.accenture.api.branch;

import co.com.accenture.api.error.GlobalErrorHandler;
import co.com.accenture.model.branch.Branch;
import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import co.com.accenture.usecase.addbranch.AddBranchUseCase;
import co.com.accenture.usecase.renamebranch.RenameBranchUseCase;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {BranchRouter.class, BranchHandler.class, GlobalErrorHandler.class})
class BranchRouterTest {

    private final Branch branch = Branch.builder().id("b1").franchiseId("f1").name("Downtown").build();

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private AddBranchUseCase addBranchUseCase;

    @MockitoBean
    private RenameBranchUseCase renameBranchUseCase;

    @Test
    void shouldAddBranchAndReturn201() {
        when(addBranchUseCase.execute("f1", "Downtown")).thenReturn(Mono.just(branch));

        post("{\"name\":\"Downtown\"}")
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json("{\"id\":\"b1\",\"franchiseId\":\"f1\",\"name\":\"Downtown\"}", true);
    }

    @Test
    void shouldReturn404WhenFranchiseDoesNotExist() {
        when(addBranchUseCase.execute("f1", "Downtown"))
                .thenReturn(Mono.error(new BusinessException(BusinessError.FRANCHISE_NOT_FOUND)));

        post("{\"name\":\"Downtown\"}")
                .expectStatus().isNotFound()
                .expectBody().json("{\"code\":\"FRANCHISE_NOT_FOUND\",\"message\":\"Franchise not found\"}", true);
    }

    @Test
    void shouldPassMissingNameToUseCaseAndReturn400() {
        when(addBranchUseCase.execute(eq("f1"), isNull()))
                .thenReturn(Mono.error(new BusinessException(BusinessError.INVALID_NAME)));

        post("{}")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("INVALID_NAME");
    }

    @Test
    void shouldReturn409WhenBranchNameExistsInFranchise() {
        when(addBranchUseCase.execute("f1", "Downtown"))
                .thenReturn(Mono.error(new BusinessException(BusinessError.BRANCH_NAME_ALREADY_EXISTS)));

        post("{\"name\":\"Downtown\"}")
                .expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.code").isEqualTo("BRANCH_NAME_ALREADY_EXISTS");
    }

    @Test
    void shouldReturn400ForMalformedJsonWithoutCallingUseCase() {
        post("{name: Downtown}")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("INVALID_REQUEST");

        verify(addBranchUseCase, never()).execute(anyString(), any());
    }

    @Test
    void shouldReturn400ForEmptyBodyWithoutCallingUseCase() {
        client.post().uri("/api/franchises/f1/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("INVALID_REQUEST");

        verify(addBranchUseCase, never()).execute(anyString(), any());
    }

    @Test
    void shouldRenameBranchAndReturn200() {
        Branch renamed = Branch.builder().id("b1").franchiseId("f1").name("Airport").build();
        when(renameBranchUseCase.execute("b1", "Airport")).thenReturn(Mono.just(renamed));

        patch("{\"name\":\"Airport\"}")
                .expectStatus().isOk()
                .expectBody().json("{\"id\":\"b1\",\"franchiseId\":\"f1\",\"name\":\"Airport\"}", true);
    }

    @Test
    void shouldReturn404WhenRenamingMissingBranch() {
        when(renameBranchUseCase.execute("b1", "Airport"))
                .thenReturn(Mono.error(new BusinessException(BusinessError.BRANCH_NOT_FOUND)));

        patch("{\"name\":\"Airport\"}")
                .expectStatus().isNotFound()
                .expectBody().json("{\"code\":\"BRANCH_NOT_FOUND\",\"message\":\"Branch not found\"}", true);
    }

    @Test
    void shouldReturn400WhenRenamingWithEmptyName() {
        when(renameBranchUseCase.execute("b1", ""))
                .thenReturn(Mono.error(new BusinessException(BusinessError.INVALID_NAME)));

        patch("{\"name\":\"\"}")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("INVALID_NAME");
    }

    @Test
    void shouldReturn409WhenRenamingToExistingName() {
        when(renameBranchUseCase.execute("b1", "Taken"))
                .thenReturn(Mono.error(new BusinessException(BusinessError.BRANCH_NAME_ALREADY_EXISTS)));

        patch("{\"name\":\"Taken\"}")
                .expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.code").isEqualTo("BRANCH_NAME_ALREADY_EXISTS");
    }

    @Test
    void shouldReturn400ForEmptyBodyOnRenameWithoutCallingUseCase() {
        client.patch().uri("/api/branches/b1/name")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("INVALID_REQUEST");

        verify(renameBranchUseCase, never()).execute(anyString(), any());
    }

    private WebTestClient.ResponseSpec post(String body) {
        return client.post().uri("/api/franchises/f1/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange();
    }

    private WebTestClient.ResponseSpec patch(String body) {
        return client.patch().uri("/api/branches/b1/name")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange();
    }
}
