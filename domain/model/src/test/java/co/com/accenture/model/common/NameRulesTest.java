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
    @ValueSource(strings = {"\u00A0Main\u00A0", "\u200BMain\u200B", "\u2003Main\u3000", "\uFEFFMain\u2060",
            "\u0007Main\u0000", "\t\u00A0\u200B Main \u200B\u00A0\n"})
    void shouldStripInvisibleCharactersAndSeparatorsAtBothEnds(String name) {
        assertThat(NameRules.normalize(name)).isEqualTo("Main");
    }

    @Test
    void shouldKeepInvisibleCharactersAndSpacesInsideTheName() {
        assertThat(NameRules.normalize(" Main\u00A0Street\u200BNorth ")).isEqualTo("Main\u00A0Street\u200BNorth");
    }

    @Test
    void shouldKeepSupplementaryCharactersAtBothEnds() {
        assertThat(NameRules.normalize(" \uD83C\uDF55 Pizza \uD83C\uDF55 ")).isEqualTo("\uD83C\uDF55 Pizza \uD83C\uDF55");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n", "\u00A0", "\u2003", "\u3000", "\u200B", "\u200C\u200D",
            "\uFEFF", "\u2060", "\u0000", "\u00A0\u200B\t\u3000"})
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
        String name = "\uD83C\uDF55".repeat(NameRules.MAX_LENGTH);

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
