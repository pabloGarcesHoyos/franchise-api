package co.com.accenture.model.common;

import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NameRulesTest {

    @Test
    void shouldStripSurroundingWhitespace() {
        assertThat(NameRules.normalize("  Main Street  ")).isEqualTo("Main Street");
    }

    @Test
    void shouldKeepAlreadyCleanName() {
        assertThat(NameRules.normalize("Main")).isEqualTo("Main");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void shouldRejectMissingOrBlankName(String name) {
        assertThatThrownBy(() -> NameRules.normalize(name))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getError())
                .isEqualTo(BusinessError.INVALID_NAME);
    }
}
