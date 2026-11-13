package co.com.accenture.model.product.gateways;

import co.com.accenture.model.product.Product;
import reactor.core.publisher.Mono;

public interface ProductRepository {

    Mono<Product> save(Product product);

    Mono<Product> findByIdAndBranchId(String id, String branchId);

    Mono<Void> deleteById(String id);

    Mono<Product> findTopByStock(String branchId);
}
