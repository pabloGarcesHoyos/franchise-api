package co.com.accenture.mongo.product;

import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import co.com.accenture.model.product.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryAdapterTest {

    private final ProductDocument unsaved = new ProductDocument(null, "b1", "Burger", 10);
    private final ProductDocument stored = new ProductDocument("p1", "b1", "Burger", 10);

    @Mock
    private ProductDataRepository repository;

    @InjectMocks
    private ProductRepositoryAdapter adapter;

    @Test
    void shouldSaveMappedDocumentAndReturnDomain() {
        when(repository.save(unsaved)).thenReturn(Mono.just(stored));

        StepVerifier.create(adapter.save(Product.create("b1", "Burger", 10)))
                .assertNext(product -> {
                    assertThat(product.getId()).isEqualTo("p1");
                    assertThat(product.getBranchId()).isEqualTo("b1");
                    assertThat(product.getName()).isEqualTo("Burger");
                    assertThat(product.getStock()).isEqualTo(10);
                })
                .verifyComplete();

        verify(repository).save(unsaved);
    }

    @Test
    void shouldTranslateDuplicateKeyToConflict() {
        when(repository.save(unsaved)).thenReturn(Mono.error(new DuplicateKeyException("E11000")));

        StepVerifier.create(adapter.save(Product.create("b1", "Burger", 10)))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.PRODUCT_NAME_ALREADY_EXISTS))
                .verify();
    }

    @Test
    void shouldPropagateOtherErrorsUnchanged() {
        DataAccessResourceFailureException failure = new DataAccessResourceFailureException("down");
        when(repository.save(unsaved)).thenReturn(Mono.error(failure));

        StepVerifier.create(adapter.save(Product.create("b1", "Burger", 10)))
                .expectErrorMatches(error -> error == failure)
                .verify();
    }

    @Test
    void shouldFindByIdAndReturnDomain() {
        when(repository.findById("p1")).thenReturn(Mono.just(stored));

        StepVerifier.create(adapter.findById("p1"))
                .assertNext(product -> assertThat(product.getName()).isEqualTo("Burger"))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenProductIsMissing() {
        when(repository.findById("p1")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById("p1"))
                .verifyComplete();
    }

    @Test
    void shouldFindByIdAndBranchIdAndReturnDomain() {
        when(repository.findByIdAndBranchId("p1", "b1")).thenReturn(Mono.just(stored));

        StepVerifier.create(adapter.findByIdAndBranchId("p1", "b1"))
                .assertNext(product -> assertThat(product.getBranchId()).isEqualTo("b1"))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenProductIsNotInBranch() {
        when(repository.findByIdAndBranchId("p1", "b2")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByIdAndBranchId("p1", "b2"))
                .verifyComplete();
    }

    @Test
    void shouldDelegateDeleteById() {
        when(repository.deleteById("p1")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteById("p1"))
                .verifyComplete();

        verify(repository).deleteById("p1");
    }

    @Test
    void shouldDelegateFindTopByStockToOrderedQuery() {
        when(repository.findFirstByBranchIdOrderByStockDescNameAsc("b1")).thenReturn(Mono.just(stored));

        StepVerifier.create(adapter.findTopByStock("b1"))
                .assertNext(product -> {
                    assertThat(product.getId()).isEqualTo("p1");
                    assertThat(product.getStock()).isEqualTo(10);
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyTopWhenBranchHasNoProducts() {
        when(repository.findFirstByBranchIdOrderByStockDescNameAsc("b1")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findTopByStock("b1"))
                .verifyComplete();
    }
}
