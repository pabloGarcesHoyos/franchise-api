package co.com.accenture.usecase.deleteproduct;

import co.com.accenture.model.branch.Branch;
import co.com.accenture.model.branch.gateways.BranchRepository;
import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import co.com.accenture.model.product.Product;
import co.com.accenture.model.product.gateways.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteProductUseCaseTest {

    private final Branch branch = Branch.builder().id("b1").franchiseId("f1").name("Downtown").build();
    private final Product product = Product.builder().id("p1").branchId("b1").name("Burger").stock(10).build();

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DeleteProductUseCase useCase;

    @Test
    void shouldDeleteProductThatBelongsToBranch() {
        when(branchRepository.findById("b1")).thenReturn(Mono.just(branch));
        when(productRepository.findByIdAndBranchId("p1", "b1")).thenReturn(Mono.just(product));
        when(productRepository.deleteById("p1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("b1", "p1"))
                .verifyComplete();

        verify(productRepository).deleteById("p1");
    }

    @Test
    void shouldFailWithBranchNotFoundWithoutLookingUpOrDeletingProduct() {
        when(branchRepository.findById("b1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("b1", "p1"))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.BRANCH_NOT_FOUND))
                .verify();

        verify(productRepository, never()).findByIdAndBranchId(anyString(), anyString());
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void shouldFailWithProductNotFoundAndNotDeleteWhenProductIsNotInBranch() {
        when(branchRepository.findById("b1")).thenReturn(Mono.just(branch));
        when(productRepository.findByIdAndBranchId("p1", "b1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("b1", "p1"))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.PRODUCT_NOT_FOUND))
                .verify();

        verify(productRepository, never()).deleteById(any());
    }
}
