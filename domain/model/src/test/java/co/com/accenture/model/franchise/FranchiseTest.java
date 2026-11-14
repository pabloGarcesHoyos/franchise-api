package co.com.accenture.model.franchise;

import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FranchiseTest {

    @Test
    void shouldCreateFranchiseWithStrippedNameAndNoId() {
        Franchise franchise = Franchise.create("  Burger House  ");

        assertThat(franchise.getId()).isNull();
        assertThat(franchise.getName()).isEqualTo("Burger House");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldRejectInvalidNameOnCreate(String name) {
        assertThatThrownBy(() -> Franchise.create(name))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getError())
                .isEqualTo(BusinessError.INVALID_NAME);
    }

    @Test
    void shouldReturnRenamedCopyKeepingIdAndOriginalUnchanged() {
        Franchise original = Franchise.builder().id("f1").name("Old").build();

        Franchise renamed = original.rename("  New  ");

        assertThat(renamed).isNotSameAs(original);
        assertThat(renamed.getId()).isEqualTo("f1");
        assertThat(renamed.getName()).isEqualTo("New");
        assertThat(original.getName()).isEqualTo("Old");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldRejectInvalidNameOnRename(String name) {
        Franchise franchise = Franchise.builder().id("f1").name("Old").build();

        assertThatThrownBy(() -> franchise.rename(name))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getError())
                .isEqualTo(BusinessError.INVALID_NAME);
    }
}
