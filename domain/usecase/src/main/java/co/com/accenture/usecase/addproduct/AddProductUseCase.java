package co.com.accenture.usecase.addproduct;

import co.com.accenture.model.branch.gateways.BranchRepository;
import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import co.com.accenture.model.product.Product;
import co.com.accenture.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AddProductUseCase {

    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public Mono<Product> execute(String branchId, String name, Integer stock) {
        return branchRepository.findById(branchId)
                .switchIfEmpty(Mono.error(() -> new BusinessException(BusinessError.BRANCH_NOT_FOUND)))
                .map(branch -> Product.create(branch.getId(), name, stock))
                .flatMap(productRepository::save);
    }
}
