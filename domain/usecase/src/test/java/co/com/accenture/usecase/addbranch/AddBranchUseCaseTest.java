package co.com.accenture.usecase.addbranch;

import co.com.accenture.model.branch.Branch;
import co.com.accenture.model.branch.gateways.BranchRepository;
import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import co.com.accenture.model.franchise.Franchise;
import co.com.accenture.model.franchise.gateways.FranchiseRepository;
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
class AddBranchUseCaseTest {

    private final Franchise franchise = Franchise.builder().id("f1").name("Burger House").build();

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private AddBranchUseCase useCase;

    @Test
    void shouldSaveBranchLinkedToFranchiseWithNormalizedName() {
        Branch saved = Branch.builder().id("b1").franchiseId("f1").name("Downtown").build();
        when(franchiseRepository.findById("f1")).thenReturn(Mono.just(franchise));
        when(branchRepository.save(any(Branch.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.execute("f1", "  Downtown  "))
                .expectNext(saved)
                .verifyComplete();

        ArgumentCaptor<Branch> captor = ArgumentCaptor.forClass(Branch.class);
        verify(branchRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
        assertThat(captor.getValue().getFranchiseId()).isEqualTo("f1");
        assertThat(captor.getValue().getName()).isEqualTo("Downtown");
    }

    @Test
    void shouldFailWithNotFoundAndNotSaveWhenFranchiseDoesNotExist() {
        when(franchiseRepository.findById("f1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("f1", "Downtown"))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.FRANCHISE_NOT_FOUND))
                .verify();

        verify(branchRepository, never()).save(any());
    }

    @Test
    void shouldFailWithValidationErrorAndNotSaveWhenNameIsBlank() {
        when(franchiseRepository.findById("f1")).thenReturn(Mono.just(franchise));

        StepVerifier.create(useCase.execute("f1", ""))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.INVALID_NAME))
                .verify();

        verify(branchRepository, never()).save(any());
    }

    @Test
    void shouldPropagateConflictFromRepository() {
        when(franchiseRepository.findById("f1")).thenReturn(Mono.just(franchise));
        when(branchRepository.save(any(Branch.class)))
                .thenReturn(Mono.error(new BusinessException(BusinessError.BRANCH_NAME_ALREADY_EXISTS)));

        StepVerifier.create(useCase.execute("f1", "Downtown"))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.BRANCH_NAME_ALREADY_EXISTS))
                .verify();
    }
}
