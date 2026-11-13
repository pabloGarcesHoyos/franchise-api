package co.com.accenture.mongo.branch;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface BranchDataRepository extends ReactiveCrudRepository<BranchDocument, String> {

    Flux<BranchDocument> findByFranchiseIdOrderByNameAsc(String franchiseId);
}
