package co.com.accenture.mongo.franchise;

import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import co.com.accenture.model.franchise.Franchise;
import co.com.accenture.model.franchise.gateways.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class FranchiseRepositoryAdapter implements FranchiseRepository {

    private final FranchiseDataRepository repository;

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return repository.save(FranchiseDocument.fromDomain(franchise))
                .map(FranchiseDocument::toDomain)
                .onErrorMap(DuplicateKeyException.class,
                        error -> new BusinessException(BusinessError.FRANCHISE_NAME_ALREADY_EXISTS));
    }

    @Override
    public Mono<Franchise> findById(String id) {
        return repository.findById(id)
                .map(FranchiseDocument::toDomain);
    }
}
