package co.com.accenture.usecase.renameproduct;

import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import co.com.accenture.model.product.Product;
import co.com.accenture.model.product.gateways.ProductRepository;
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
class RenameProductUseCaseTest {

    private final Product existing = Product.builder().id("p1").branchId("b1").name("Old").stock(10).build();

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private RenameProductUseCase useCase;

    @Test
    void shouldSaveRenamedProduct() {
        Product saved = Product.builder().id("p1").branchId("b1").name("New").stock(10).build();
        when(productRepository.findById("p1")).thenReturn(Mono.just(existing));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.execute("p1", "  New  "))
                .expectNext(saved)
                .verifyComplete();

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo("p1");
        assertThat(captor.getValue().getBranchId()).isEqualTo("b1");
        assertThat(captor.getValue().getName()).isEqualTo("New");
        assertThat(captor.getValue().getStock()).isEqualTo(10);
    }

    @Test
    void shouldFailWithNotFoundAndNotSaveWhenProductDoesNotExist() {
        when(productRepository.findById("p1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("p1", "New"))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.PRODUCT_NOT_FOUND))
                .verify();

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldFailWithValidationErrorAndNotSaveWhenNameIsBlank() {
        when(productRepository.findById("p1")).thenReturn(Mono.just(existing));

        StepVerifier.create(useCase.execute("p1", ""))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.INVALID_NAME))
                .verify();

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldPropagateConflictFromRepository() {
        when(productRepository.findById("p1")).thenReturn(Mono.just(existing));
        when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.error(new BusinessException(BusinessError.PRODUCT_NAME_ALREADY_EXISTS)));

        StepVerifier.create(useCase.execute("p1", "Taken"))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.PRODUCT_NAME_ALREADY_EXISTS))
                .verify();
    }
}
