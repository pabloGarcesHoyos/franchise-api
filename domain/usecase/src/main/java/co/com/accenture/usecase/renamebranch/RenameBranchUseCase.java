package co.com.accenture.usecase.renamebranch;

import co.com.accenture.model.branch.Branch;
import co.com.accenture.model.branch.gateways.BranchRepository;
import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RenameBranchUseCase {

    private final BranchRepository branchRepository;

    public Mono<Branch> execute(String branchId, String name) {
        return branchRepository.findById(branchId)
                .switchIfEmpty(Mono.error(() -> new BusinessException(BusinessError.BRANCH_NOT_FOUND)))
                .map(branch -> branch.rename(name))
                .flatMap(branchRepository::save);
    }
}
