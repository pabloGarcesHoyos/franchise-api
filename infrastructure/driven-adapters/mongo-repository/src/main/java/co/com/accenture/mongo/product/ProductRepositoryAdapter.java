package co.com.accenture.mongo.product;

import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import co.com.accenture.model.product.Product;
import co.com.accenture.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductDataRepository repository;

    @Override
    public Mono<Product> save(Product product) {
        return repository.save(ProductDocument.fromDomain(product))
                .map(ProductDocument::toDomain)
                .onErrorMap(DuplicateKeyException.class,
                        error -> new BusinessException(BusinessError.PRODUCT_NAME_ALREADY_EXISTS));
    }

    @Override
    public Mono<Product> findById(String id) {
        return repository.findById(id)
                .map(ProductDocument::toDomain);
    }

    @Override
    public Mono<Product> findByIdAndBranchId(String id, String branchId) {
        return repository.findByIdAndBranchId(id, branchId)
                .map(ProductDocument::toDomain);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Product> findTopByStock(String branchId) {
        return repository.findFirstByBranchIdOrderByStockDescNameAsc(branchId)
                .map(ProductDocument::toDomain);
    }
}
