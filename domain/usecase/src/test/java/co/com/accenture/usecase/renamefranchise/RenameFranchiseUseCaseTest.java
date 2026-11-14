package co.com.accenture.usecase.renamefranchise;

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
class RenameFranchiseUseCaseTest {

    private final Franchise existing = Franchise.builder().id("f1").name("Old").build();

    @Mock
    private FranchiseRepository franchiseRepository;

    @InjectMocks
    private RenameFranchiseUseCase useCase;

    @Test
    void shouldSaveRenamedFranchise() {
        Franchise saved = Franchise.builder().id("f1").name("New").build();
        when(franchiseRepository.findById("f1")).thenReturn(Mono.just(existing));
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.execute("f1", "  New  "))
                .expectNext(saved)
                .verifyComplete();

        ArgumentCaptor<Franchise> captor = ArgumentCaptor.forClass(Franchise.class);
        verify(franchiseRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo("f1");
        assertThat(captor.getValue().getName()).isEqualTo("New");
    }

    @Test
    void shouldFailWithNotFoundAndNotSaveWhenFranchiseDoesNotExist() {
        when(franchiseRepository.findById("f1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("f1", "New"))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.FRANCHISE_NOT_FOUND))
                .verify();

        verify(franchiseRepository, never()).save(any());
    }

    @Test
    void shouldFailWithValidationErrorAndNotSaveWhenNameIsBlank() {
        when(franchiseRepository.findById("f1")).thenReturn(Mono.just(existing));

        StepVerifier.create(useCase.execute("f1", " "))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.INVALID_NAME))
                .verify();

        verify(franchiseRepository, never()).save(any());
    }

    @Test
    void shouldPropagateConflictFromRepository() {
        when(franchiseRepository.findById("f1")).thenReturn(Mono.just(existing));
        when(franchiseRepository.save(any(Franchise.class)))
                .thenReturn(Mono.error(new BusinessException(BusinessError.FRANCHISE_NAME_ALREADY_EXISTS)));

        StepVerifier.create(useCase.execute("f1", "Taken"))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.FRANCHISE_NAME_ALREADY_EXISTS))
                .verify();
    }
}
