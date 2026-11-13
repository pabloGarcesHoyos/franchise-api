package co.com.accenture.mongo.franchise;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface FranchiseDataRepository extends ReactiveCrudRepository<FranchiseDocument, String> {
}
