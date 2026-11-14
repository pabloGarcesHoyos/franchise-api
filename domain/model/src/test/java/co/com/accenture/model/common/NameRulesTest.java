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
    @ValueSource(strings = {" Main ", "​Main​", " Main　", "﻿Main⁠",
            "\u0007Main\u0000", "\t ​ Main ​ \n"})
    void shouldStripInvisibleCharactersAndSeparatorsAtBothEnds(String name) {
        assertThat(NameRules.normalize(name)).isEqualTo("Main");
    }

    @Test
    void shouldKeepInvisibleCharactersAndSpacesInsideTheName() {
        assertThat(NameRules.normalize(" Main Street​North ")).isEqualTo("Main Street​North");
    }

    @Test
    void shouldKeepSupplementaryCharactersAtBothEnds() {
        assertThat(NameRules.normalize(" 🍕 Pizza 🍕 ")).isEqualTo("🍕 Pizza 🍕");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n", " ", " ", "　", "​", "‌‍",
            "﻿", "⁠", "\u0000", " ​\t　"})
    void shouldRejectMissingBlankOrInvisibleOnlyName(String name) {
        assertThatThrownBy(() -> NameRules.normalize(name))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getError())
                .isEqualTo(BusinessError.INVALID_NAME);
    }

    @Test
    void shouldAcceptNameWithMaximumLength() {
        String name = "n".repeat(NameRules.MAX_LENGTH);

        assertThat(NameRules.normalize("  " + name + "  ")).isEqualTo(name);
    }

    @Test
    void shouldCountSupplementaryCharactersAsOneCharacter() {
        String name = "🍕".repeat(NameRules.MAX_LENGTH);

        assertThat(NameRules.normalize(name)).isEqualTo(name);
    }

    @ParameterizedTest
    @ValueSource(ints = {NameRules.MAX_LENGTH + 1, 100_000})
    void shouldRejectNameLongerThanMaximumLength(int length) {
        assertThatThrownBy(() -> NameRules.normalize("n".repeat(length)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getError())
                .isEqualTo(BusinessError.NAME_TOO_LONG);
    }

    @Test
    void shouldExposeMaximumLength() {
        assertThat(NameRules.MAX_LENGTH).isEqualTo(100);
    }
}
