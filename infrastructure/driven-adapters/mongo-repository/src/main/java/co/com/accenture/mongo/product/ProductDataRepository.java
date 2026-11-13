package co.com.accenture.mongo.product;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ProductDataRepository extends ReactiveCrudRepository<ProductDocument, String> {

    Mono<ProductDocument> findByIdAndBranchId(String id, String branchId);

    Mono<ProductDocument> findFirstByBranchIdOrderByStockDescNameAsc(String branchId);
}
