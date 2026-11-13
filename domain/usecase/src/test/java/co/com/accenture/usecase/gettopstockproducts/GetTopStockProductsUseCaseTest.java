package co.com.accenture.usecase.gettopstockproducts;

import co.com.accenture.model.branch.Branch;
import co.com.accenture.model.branch.gateways.BranchRepository;
import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import co.com.accenture.model.franchise.Franchise;
import co.com.accenture.model.franchise.gateways.FranchiseRepository;
import co.com.accenture.model.product.Product;
import co.com.accenture.model.product.TopStockProduct;
import co.com.accenture.model.product.gateways.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTopStockProductsUseCaseTest {

    private final Franchise franchise = Franchise.builder().id("f1").name("Burger House").build();
    private final Branch downtown = Branch.builder().id("b1").franchiseId("f1").name("Downtown").build();
    private final Branch airport = Branch.builder().id("b2").franchiseId("f1").name("Airport").build();
    private final Branch mall = Branch.builder().id("b3").franchiseId("f1").name("Mall").build();

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private GetTopStockProductsUseCase useCase;

    @Test
    void shouldReturnTopStockProductOfEachBranchInBranchOrder() {
        when(franchiseRepository.findById("f1")).thenReturn(Mono.just(franchise));
        when(branchRepository.findByFranchiseId("f1")).thenReturn(Flux.just(downtown, airport));
        when(productRepository.findTopByStock("b1")).thenReturn(Mono.just(product("p1", "b1", "Burger", 40))
                .delayElement(Duration.ofMillis(50)));
        when(productRepository.findTopByStock("b2")).thenReturn(Mono.just(product("p2", "b2", "Fries", 15)));

        StepVerifier.create(useCase.execute("f1"))
                .expectNext(new TopStockProduct("b1", "Downtown", "p1", "Burger", 40))
                .expectNext(new TopStockProduct("b2", "Airport", "p2", "Fries", 15))
                .verifyComplete();
    }

    @Test
    void shouldSkipBranchesWithoutProducts() {
        when(franchiseRepository.findById("f1")).thenReturn(Mono.just(franchise));
        when(branchRepository.findByFranchiseId("f1")).thenReturn(Flux.just(downtown, airport, mall));
        when(productRepository.findTopByStock("b1")).thenReturn(Mono.just(product("p1", "b1", "Burger", 40)));
        when(productRepository.findTopByStock("b2")).thenReturn(Mono.empty());
        when(productRepository.findTopByStock("b3")).thenReturn(Mono.just(product("p3", "b3", "Soda", 8)));

        StepVerifier.create(useCase.execute("f1"))
                .expectNext(new TopStockProduct("b1", "Downtown", "p1", "Burger", 40))
                .expectNext(new TopStockProduct("b3", "Mall", "p3", "Soda", 8))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenFranchiseHasNoBranches() {
        when(franchiseRepository.findById("f1")).thenReturn(Mono.just(franchise));
        when(branchRepository.findByFranchiseId("f1")).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute("f1"))
                .verifyComplete();

        verify(productRepository, never()).findTopByStock(anyString());
    }

    @Test
    void shouldFailWithNotFoundWithoutQueryingBranchesWhenFranchiseDoesNotExist() {
        when(franchiseRepository.findById("f1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("f1"))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.FRANCHISE_NOT_FOUND))
                .verify();

        verify(branchRepository, never()).findByFranchiseId(anyString());
        verify(productRepository, never()).findTopByStock(anyString());
    }

    private Product product(String id, String branchId, String name, int stock) {
        return Product.builder().id(id).branchId(branchId).name(name).stock(stock).build();
    }
}
