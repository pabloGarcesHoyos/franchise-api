package co.com.accenture.usecase.createfranchise;

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
class CreateFranchiseUseCaseTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @InjectMocks
    private CreateFranchiseUseCase useCase;

    @Test
    void shouldSaveFranchiseWithNormalizedName() {
        Franchise saved = Franchise.builder().id("f1").name("Burger House").build();
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.execute("  Burger House  "))
                .expectNext(saved)
                .verifyComplete();

        ArgumentCaptor<Franchise> captor = ArgumentCaptor.forClass(Franchise.class);
        verify(franchiseRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
        assertThat(captor.getValue().getName()).isEqualTo("Burger House");
    }

    @Test
    void shouldFailWithValidationErrorAndNotSaveWhenNameIsBlank() {
        StepVerifier.create(useCase.execute("   "))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.INVALID_NAME))
                .verify();

        verify(franchiseRepository, never()).save(any());
    }

    @Test
    void shouldPropagateConflictFromRepository() {
        when(franchiseRepository.save(any(Franchise.class)))
                .thenReturn(Mono.error(new BusinessException(BusinessError.FRANCHISE_NAME_ALREADY_EXISTS)));

        StepVerifier.create(useCase.execute("Burger House"))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.FRANCHISE_NAME_ALREADY_EXISTS))
                .verify();
    }
}
