package co.com.accenture.usecase.gettopstockproducts;

import co.com.accenture.model.branch.Branch;
import co.com.accenture.model.branch.gateways.BranchRepository;
import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import co.com.accenture.model.franchise.gateways.FranchiseRepository;
import co.com.accenture.model.product.TopStockProduct;
import co.com.accenture.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetTopStockProductsUseCase {

    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public Flux<TopStockProduct> execute(String franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(() -> new BusinessException(BusinessError.FRANCHISE_NOT_FOUND)))
                .flatMapMany(franchise -> branchRepository.findByFranchiseId(franchise.getId()))
                .flatMapSequential(this::findTopStockProduct);
    }

    private Mono<TopStockProduct> findTopStockProduct(Branch branch) {
        return productRepository.findTopByStock(branch.getId())
                .map(product -> new TopStockProduct(
                        branch.getId(),
                        branch.getName(),
                        product.getId(),
                        product.getName(),
                        product.getStock()));
    }
}
