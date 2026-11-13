package co.com.accenture.mongo.branch;

import co.com.accenture.model.branch.Branch;
import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchRepositoryAdapterTest {

    private final BranchDocument unsaved = new BranchDocument(null, "f1", "Downtown");
    private final BranchDocument stored = new BranchDocument("b1", "f1", "Downtown");

    @Mock
    private BranchDataRepository repository;

    @InjectMocks
    private BranchRepositoryAdapter adapter;

    @Test
    void shouldSaveMappedDocumentAndReturnDomain() {
        when(repository.save(unsaved)).thenReturn(Mono.just(stored));

        StepVerifier.create(adapter.save(Branch.create("f1", "Downtown")))
                .assertNext(branch -> {
                    assertThat(branch.getId()).isEqualTo("b1");
                    assertThat(branch.getFranchiseId()).isEqualTo("f1");
                    assertThat(branch.getName()).isEqualTo("Downtown");
                })
                .verifyComplete();

        verify(repository).save(unsaved);
    }

    @Test
    void shouldTranslateDuplicateKeyToConflict() {
        when(repository.save(unsaved)).thenReturn(Mono.error(new DuplicateKeyException("E11000")));

        StepVerifier.create(adapter.save(Branch.create("f1", "Downtown")))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.BRANCH_NAME_ALREADY_EXISTS))
                .verify();
    }

    @Test
    void shouldPropagateOtherErrorsUnchanged() {
        DataAccessResourceFailureException failure = new DataAccessResourceFailureException("down");
        when(repository.save(unsaved)).thenReturn(Mono.error(failure));

        StepVerifier.create(adapter.save(Branch.create("f1", "Downtown")))
                .expectErrorMatches(error -> error == failure)
                .verify();
    }

    @Test
    void shouldFindByIdAndReturnDomain() {
        when(repository.findById("b1")).thenReturn(Mono.just(stored));

        StepVerifier.create(adapter.findById("b1"))
                .assertNext(branch -> assertThat(branch.getName()).isEqualTo("Downtown"))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenBranchIsMissing() {
        when(repository.findById("b1")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById("b1"))
                .verifyComplete();
    }

    @Test
    void shouldDelegateFindByFranchiseIdToOrderedQueryKeepingOrder() {
        when(repository.findByFranchiseIdOrderByNameAsc("f1")).thenReturn(Flux.just(
                new BranchDocument("b2", "f1", "Airport"),
                new BranchDocument("b1", "f1", "Downtown")));

        StepVerifier.create(adapter.findByFranchiseId("f1").map(Branch::getName))
                .expectNext("Airport", "Downtown")
                .verifyComplete();
    }
}
