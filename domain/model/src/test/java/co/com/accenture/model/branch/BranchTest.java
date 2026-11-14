package co.com.accenture.model.branch;

import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BranchTest {

    @Test
    void shouldCreateBranchForFranchiseWithStrippedNameAndNoId() {
        Branch branch = Branch.create("f1", "  Downtown  ");

        assertThat(branch.getId()).isNull();
        assertThat(branch.getFranchiseId()).isEqualTo("f1");
        assertThat(branch.getName()).isEqualTo("Downtown");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldRejectInvalidNameOnCreate(String name) {
        assertThatThrownBy(() -> Branch.create("f1", name))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getError())
                .isEqualTo(BusinessError.INVALID_NAME);
    }

    @Test
    void shouldReturnRenamedCopyKeepingIdsAndOriginalUnchanged() {
        Branch original = Branch.builder().id("b1").franchiseId("f1").name("Old").build();

        Branch renamed = original.rename("  New  ");

        assertThat(renamed).isNotSameAs(original);
        assertThat(renamed.getId()).isEqualTo("b1");
        assertThat(renamed.getFranchiseId()).isEqualTo("f1");
        assertThat(renamed.getName()).isEqualTo("New");
        assertThat(original.getName()).isEqualTo("Old");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldRejectInvalidNameOnRename(String name) {
        Branch branch = Branch.builder().id("b1").franchiseId("f1").name("Old").build();

        assertThatThrownBy(() -> branch.rename(name))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getError())
                .isEqualTo(BusinessError.INVALID_NAME);
    }
}
