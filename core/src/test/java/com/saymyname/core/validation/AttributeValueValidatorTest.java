package com.saymyname.core.validation;

import com.saymyname.core.model.people.AttributeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeValueValidatorTest {

    @Test
    void isValid_textType_acceptsAnyNonBlankValue() {
        assertThat(AttributeValueValidator.isValid("hello", AttributeType.TEXT)).isTrue();
        assertThat(AttributeValueValidator.isValid("123", AttributeType.TEXT)).isTrue();
        assertThat(AttributeValueValidator.isValid("Jean-Marie O'Connor", AttributeType.TEXT)).isTrue();
    }

    @Test
    void isValid_textType_rejectsNullAndBlank() {
        assertThat(AttributeValueValidator.isValid(null, AttributeType.TEXT)).isFalse();
        assertThat(AttributeValueValidator.isValid("", AttributeType.TEXT)).isFalse();
        assertThat(AttributeValueValidator.isValid("   ", AttributeType.TEXT)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "-456", "0", "3.14", "-2.5", "0.001"})
    void isValid_numberType_acceptsValidNumbers(String value) {
        assertThat(AttributeValueValidator.isValid(value, AttributeType.NUMBER)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "12.34.56", "1,234", "12a", "3.14.15"})
    void isValid_numberType_rejectsInvalidNumbers(String value) {
        assertThat(AttributeValueValidator.isValid(value, AttributeType.NUMBER)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2024-01-15", "2000-12-31", "1990-06-01"})
    void isValid_dateType_acceptsValidDates(String value) {
        assertThat(AttributeValueValidator.isValid(value, AttributeType.DATE)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2024-13-01", "2024-01-32", "01/01/2024", "2024/01/01", "not-a-date"})
    void isValid_dateType_rejectsInvalidDates(String value) {
        assertThat(AttributeValueValidator.isValid(value, AttributeType.DATE)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2024-01-15T10:30:00", "2000-12-31T23:59:59", "1990-06-01T00:00:00"})
    void isValid_datetimeType_acceptsValidDateTimes(String value) {
        assertThat(AttributeValueValidator.isValid(value, AttributeType.DATETIME)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2024-01-15", "2024-01-15 10:30:00", "not-a-datetime", "2024-01-15T25:00:00"})
    void isValid_datetimeType_rejectsInvalidDateTimes(String value) {
        assertThat(AttributeValueValidator.isValid(value, AttributeType.DATETIME)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false", "TRUE", "FALSE", "True", "False"})
    void isValid_booleanType_acceptsValidBooleans(String value) {
        assertThat(AttributeValueValidator.isValid(value, AttributeType.BOOLEAN)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"yes", "no", "1", "0", "T", "F", "truee"})
    void isValid_booleanType_rejectsInvalidBooleans(String value) {
        assertThat(AttributeValueValidator.isValid(value, AttributeType.BOOLEAN)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"user@example.com", "test.user+tag@domain.co.uk", "a@b.c"})
    void isValid_emailType_acceptsValidEmails(String value) {
        assertThat(AttributeValueValidator.isValid(value, AttributeType.EMAIL)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "@example.com", "user@", "user @example.com"})
    void isValid_emailType_rejectsInvalidEmails(String value) {
        assertThat(AttributeValueValidator.isValid(value, AttributeType.EMAIL)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://example.com",
            "http://example.com",
            "example.com",
            "www.example.com",
            "https://example.com/path?query=value",
            "subdomain.example.com"
    })
    void isValid_urlType_acceptsValidUrls(String value) {
        assertThat(AttributeValueValidator.isValid(value, AttributeType.URL)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"not a url", "ht!tp://invalid", "just-text"})
    void isValid_urlType_rejectsInvalidUrls(String value) {
        assertThat(AttributeValueValidator.isValid(value, AttributeType.URL)).isFalse();
    }

    @Test
    void isValid_enumType_acceptsAnyNonBlankValue() {
        // ENUM validation is deferred to constraint checking
        assertThat(AttributeValueValidator.isValid("OPTION_A", AttributeType.ENUM)).isTrue();
        assertThat(AttributeValueValidator.isValid("anything", AttributeType.ENUM)).isTrue();
    }

    @Test
    void isValid_allTypes_rejectNullAndBlank() {
        for (AttributeType type : AttributeType.values()) {
            assertThat(AttributeValueValidator.isValid(null, type))
                    .as("Type %s should reject null", type)
                    .isFalse();
            assertThat(AttributeValueValidator.isValid("", type))
                    .as("Type %s should reject empty string", type)
                    .isFalse();
            assertThat(AttributeValueValidator.isValid("   ", type))
                    .as("Type %s should reject blank string", type)
                    .isFalse();
        }
    }
}
