package co.com.accenture.mongo.branch;

import co.com.accenture.model.branch.Branch;
import co.com.accenture.model.branch.gateways.BranchRepository;
import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class BranchRepositoryAdapter implements BranchRepository {

    private final BranchDataRepository repository;

    @Override
    public Mono<Branch> save(Branch branch) {
        return repository.save(BranchDocument.fromDomain(branch))
                .map(BranchDocument::toDomain)
                .onErrorMap(DuplicateKeyException.class,
                        error -> new BusinessException(BusinessError.BRANCH_NAME_ALREADY_EXISTS));
    }

    @Override
    public Mono<Branch> findById(String id) {
        return repository.findById(id)
                .map(BranchDocument::toDomain);
    }

    @Override
    public Flux<Branch> findByFranchiseId(String franchiseId) {
        return repository.findByFranchiseIdOrderByNameAsc(franchiseId)
                .map(BranchDocument::toDomain);
    }
}
