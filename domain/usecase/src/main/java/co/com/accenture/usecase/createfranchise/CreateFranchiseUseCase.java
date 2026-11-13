package co.com.accenture.usecase.createfranchise;

import co.com.accenture.model.franchise.Franchise;
import co.com.accenture.model.franchise.gateways.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CreateFranchiseUseCase {

    private final FranchiseRepository franchiseRepository;

    public Mono<Franchise> execute(String name) {
        return Mono.fromCallable(() -> Franchise.create(name))
                .flatMap(franchiseRepository::save);
    }
}
