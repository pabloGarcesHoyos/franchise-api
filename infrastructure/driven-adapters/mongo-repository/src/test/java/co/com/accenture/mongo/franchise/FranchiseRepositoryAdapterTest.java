package co.com.accenture.mongo.franchise;

import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import co.com.accenture.model.franchise.Franchise;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseRepositoryAdapterTest {

    private final FranchiseDocument unsaved = new FranchiseDocument(null, "Burger House");
    private final FranchiseDocument stored = new FranchiseDocument("f1", "Burger House");

    @Mock
    private FranchiseDataRepository repository;

    @InjectMocks
    private FranchiseRepositoryAdapter adapter;

    @Test
    void shouldSaveMappedDocumentAndReturnDomain() {
        when(repository.save(unsaved)).thenReturn(Mono.just(stored));

        StepVerifier.create(adapter.save(Franchise.create("Burger House")))
                .assertNext(franchise -> {
                    assertThat(franchise.getId()).isEqualTo("f1");
                    assertThat(franchise.getName()).isEqualTo("Burger House");
                })
                .verifyComplete();

        verify(repository).save(unsaved);
    }

    @Test
    void shouldTranslateDuplicateKeyToConflict() {
        when(repository.save(unsaved)).thenReturn(Mono.error(new DuplicateKeyException("E11000")));

        StepVerifier.create(adapter.save(Franchise.create("Burger House")))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.FRANCHISE_NAME_ALREADY_EXISTS))
                .verify();
    }

    @Test
    void shouldPropagateOtherErrorsUnchanged() {
        DataAccessResourceFailureException failure = new DataAccessResourceFailureException("down");
        when(repository.save(unsaved)).thenReturn(Mono.error(failure));

        StepVerifier.create(adapter.save(Franchise.create("Burger House")))
                .expectErrorMatches(error -> error == failure)
                .verify();
    }

    @Test
    void shouldFindByIdAndReturnDomain() {
        when(repository.findById("f1")).thenReturn(Mono.just(stored));

        StepVerifier.create(adapter.findById("f1"))
                .assertNext(franchise -> {
                    assertThat(franchise.getId()).isEqualTo("f1");
                    assertThat(franchise.getName()).isEqualTo("Burger House");
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenFranchiseIsMissing() {
        when(repository.findById("f1")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById("f1"))
                .verifyComplete();
    }
}
