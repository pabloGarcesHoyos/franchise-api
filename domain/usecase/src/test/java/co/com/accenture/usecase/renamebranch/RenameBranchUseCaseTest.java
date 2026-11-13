package co.com.accenture.usecase.renamebranch;

import co.com.accenture.model.branch.Branch;
import co.com.accenture.model.branch.gateways.BranchRepository;
import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RenameBranchUseCaseTest {

    private final Branch existing = Branch.builder().id("b1").franchiseId("f1").name("Old").build();

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private RenameBranchUseCase useCase;

    @Test
    void shouldSaveRenamedBranch() {
        Branch saved = Branch.builder().id("b1").franchiseId("f1").name("New").build();
        when(branchRepository.findById("b1")).thenReturn(Mono.just(existing));
        when(branchRepository.save(any(Branch.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.execute("b1", "  New  "))
                .expectNext(saved)
                .verifyComplete();

        ArgumentCaptor<Branch> captor = ArgumentCaptor.forClass(Branch.class);
        verify(branchRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo("b1");
        assertThat(captor.getValue().getFranchiseId()).isEqualTo("f1");
        assertThat(captor.getValue().getName()).isEqualTo("New");
    }

    @Test
    void shouldFailWithNotFoundAndNotSaveWhenBranchDoesNotExist() {
        when(branchRepository.findById("b1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("b1", "New"))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.BRANCH_NOT_FOUND))
                .verify();

        verify(branchRepository, never()).save(any());
    }

    @Test
    void shouldFailWithValidationErrorAndNotSaveWhenNameIsBlank() {
        when(branchRepository.findById("b1")).thenReturn(Mono.just(existing));

        StepVerifier.create(useCase.execute("b1", "  "))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.INVALID_NAME))
                .verify();

        verify(branchRepository, never()).save(any());
    }

    @Test
    void shouldPropagateConflictFromRepository() {
        when(branchRepository.findById("b1")).thenReturn(Mono.just(existing));
        when(branchRepository.save(any(Branch.class)))
                .thenReturn(Mono.error(new BusinessException(BusinessError.BRANCH_NAME_ALREADY_EXISTS)));

        StepVerifier.create(useCase.execute("b1", "Taken"))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.BRANCH_NAME_ALREADY_EXISTS))
                .verify();
    }
}
