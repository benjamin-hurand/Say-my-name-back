package com.saymyname.core.util;

import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.people.ValueType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class TextNormalizationTest {

    @Test
    void softNormalizeWhitespace_trimAndCollapseSpaces() {
        assertThat(TextNormalization.softNormalizeWhitespace("  hello   world  "))
                .isEqualTo("hello world");
    }

    @Test
    void softNormalizeWhitespace_handlesNull() {
        assertThat(TextNormalization.softNormalizeWhitespace(null)).isNull();
    }

    @Test
    void softNormalizeWhitespace_preservesCase() {
        assertThat(TextNormalization.softNormalizeWhitespace("  HeLLo   WoRLd  "))
                .isEqualTo("HeLLo WoRLd");
    }

    @ParameterizedTest
    @CsvSource({
            "jean-marie, TITLE_CASE, Jean-Marie",
            "jean-marie, UPPERCASE, JEAN-MARIE",
            "jean-marie, SENTENCE_PRESERVE, Jean-marie",
            "jean-marie, NONE, jean-marie",
            "o'connor, TITLE_CASE, O'Connor",
            "o'CONNOR, SENTENCE_PRESERVE, O'CONNOR"
    })
    void applyCasing_appliesCorrectStrategy(String input, CasingStrategy strategy, String expected) {
        assertThat(TextNormalization.applyCasing(input, strategy)).isEqualTo(expected);
    }

    @Test
    void applyCasing_handlesNull() {
        assertThat(TextNormalization.applyCasing(null, CasingStrategy.TITLE_CASE)).isNull();
    }

    @Test
    void applyCasing_defaultsToNoneWhenStrategyNull() {
        assertThat(TextNormalization.applyCasing("hello", null)).isEqualTo("hello");
    }

    @ParameterizedTest
    @CsvSource({
            "jean-marie dupont, Jean-Marie Dupont",
            "o'connor, O'Connor",
            "john, John",
            "UPPERCASE, Uppercase",
            "marie-josé o'brian, Marie-José O'Brian"
    })
    void titleCaseSimple_capitalizesProperly(String input, String expected) {
        assertThat(TextNormalization.titleCaseSimple(input)).isEqualTo(expected);
    }

    @Test
    void titleCaseSimple_handlesNull() {
        assertThat(TextNormalization.titleCaseSimple(null)).isNull();
    }

    @Test
    void titleCaseSimple_handlesEmpty() {
        assertThat(TextNormalization.titleCaseSimple("")).isEmpty();
    }

    @Test
    void capitalizeFirstLetterPreserveRest_preservesRest() {
        assertThat(TextNormalization.capitalizeFirstLetterPreserveRest("john")).isEqualTo("John");
        assertThat(TextNormalization.capitalizeFirstLetterPreserveRest("JOHN")).isEqualTo("JOHN");
        assertThat(TextNormalization.capitalizeFirstLetterPreserveRest("éléonore")).isEqualTo("Éléonore");
        assertThat(TextNormalization.capitalizeFirstLetterPreserveRest("123abc")).isEqualTo("123Abc");
    }

    @Test
    void capitalizeFirstLetterPreserveRest_handlesNull() {
        assertThat(TextNormalization.capitalizeFirstLetterPreserveRest(null)).isNull();
    }

    @Test
    void normalizeWithStrategy_appliesOnlyToTextType() {
        String input = "  john doe  ";

        // TEXT type applies strategy
        assertThat(TextNormalization.normalizeWithStrategy(input, ValueType.TEXT, CasingStrategy.TITLE_CASE))
                .isEqualTo("John Doe");

        // NUMBER type ignores strategy, only normalizes whitespace
        assertThat(TextNormalization.normalizeWithStrategy(input, ValueType.NUMBER, CasingStrategy.TITLE_CASE))
                .isEqualTo("john doe");

        // DATE type ignores strategy
        assertThat(TextNormalization.normalizeWithStrategy(input, ValueType.DATE, CasingStrategy.UPPERCASE))
                .isEqualTo("john doe");
    }

    @Test
    void normalizeWithStrategy_handlesNull() {
        assertThat(TextNormalization.normalizeWithStrategy(null, ValueType.TEXT, CasingStrategy.TITLE_CASE))
                .isNull();
    }

    @Test
    void normalizeWithStrategy_handlesBlank() {
        String blank = "   ";
        assertThat(TextNormalization.normalizeWithStrategy(blank, ValueType.TEXT, CasingStrategy.TITLE_CASE))
                .isEqualTo("");
    }

    @SuppressWarnings("deprecation")
    @Test
    void normalizeForStorage_legacyBehavior() {
        assertThat(TextNormalization.normalizeForStorage("  jean-marie  "))
                .isEqualTo("Jean-Marie");
    }

    @SuppressWarnings("deprecation")
    @Test
    void normalizeForStorage_handlesNull() {
        assertThat(TextNormalization.normalizeForStorage(null)).isNull();
    }

    @SuppressWarnings("deprecation")
    @Test
    void normalizeForStorage_handlesEmpty() {
        assertThat(TextNormalization.normalizeForStorage("   ")).isEmpty();
    }
}
