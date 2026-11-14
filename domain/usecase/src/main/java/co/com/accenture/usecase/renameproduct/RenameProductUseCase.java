package co.com.accenture.usecase.renameproduct;

import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import co.com.accenture.model.product.Product;
import co.com.accenture.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RenameProductUseCase {

    private final ProductRepository productRepository;

    public Mono<Product> execute(String productId, String name) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(() -> new BusinessException(BusinessError.PRODUCT_NOT_FOUND)))
                .map(product -> product.rename(name))
                .flatMap(productRepository::save);
    }
}
