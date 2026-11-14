package co.com.accenture.usecase.deleteproduct;

import co.com.accenture.model.branch.gateways.BranchRepository;
import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import co.com.accenture.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DeleteProductUseCase {

    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public Mono<Void> execute(String branchId, String productId) {
        return branchRepository.findById(branchId)
                .switchIfEmpty(Mono.error(() -> new BusinessException(BusinessError.BRANCH_NOT_FOUND)))
                .flatMap(branch -> productRepository.findByIdAndBranchId(productId, branch.getId()))
                .switchIfEmpty(Mono.error(() -> new BusinessException(BusinessError.PRODUCT_NOT_FOUND)))
                .flatMap(product -> productRepository.deleteById(product.getId()));
    }
}
