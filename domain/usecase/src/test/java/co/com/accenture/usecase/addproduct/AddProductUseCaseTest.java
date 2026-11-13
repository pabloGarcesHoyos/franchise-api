package co.com.accenture.usecase.addproduct;

import co.com.accenture.model.branch.Branch;
import co.com.accenture.model.branch.gateways.BranchRepository;
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
class AddProductUseCaseTest {

    private final Branch branch = Branch.builder().id("b1").franchiseId("f1").name("Downtown").build();

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private AddProductUseCase useCase;

    @Test
    void shouldSaveProductLinkedToBranch() {
        Product saved = Product.builder().id("p1").branchId("b1").name("Burger").stock(10).build();
        when(branchRepository.findById("b1")).thenReturn(Mono.just(branch));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.execute("b1", "  Burger  ", 10))
                .expectNext(saved)
                .verifyComplete();

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
        assertThat(captor.getValue().getBranchId()).isEqualTo("b1");
        assertThat(captor.getValue().getName()).isEqualTo("Burger");
        assertThat(captor.getValue().getStock()).isEqualTo(10);
    }

    @Test
    void shouldFailWithNotFoundAndNotSaveWhenBranchDoesNotExist() {
        when(branchRepository.findById("b1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("b1", "Burger", 10))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.BRANCH_NOT_FOUND))
                .verify();

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldFailWithValidationErrorAndNotSaveWhenNameIsBlank() {
        when(branchRepository.findById("b1")).thenReturn(Mono.just(branch));

        StepVerifier.create(useCase.execute("b1", " ", 10))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.INVALID_NAME))
                .verify();

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldFailWithValidationErrorAndNotSaveWhenStockIsNegative() {
        when(branchRepository.findById("b1")).thenReturn(Mono.just(branch));

        StepVerifier.create(useCase.execute("b1", "Burger", -1))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.INVALID_STOCK))
                .verify();

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldPropagateConflictFromRepository() {
        when(branchRepository.findById("b1")).thenReturn(Mono.just(branch));
        when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.error(new BusinessException(BusinessError.PRODUCT_NAME_ALREADY_EXISTS)));

        StepVerifier.create(useCase.execute("b1", "Burger", 10))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.PRODUCT_NAME_ALREADY_EXISTS))
                .verify();
    }

    @Test
    void shouldFailWithValidationErrorAndNotSaveWhenStockIsMissing() {
        when(branchRepository.findById("b1")).thenReturn(Mono.just(branch));

        StepVerifier.create(useCase.execute("b1", "Burger", null))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.INVALID_STOCK))
                .verify();

        verify(productRepository, never()).save(any());
    }
}
