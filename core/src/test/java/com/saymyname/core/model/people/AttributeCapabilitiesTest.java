package com.saymyname.core.model.people;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.junit.jupiter.api.Test;

class AttributeCapabilitiesTest {

    @Test
    void textIsNeitherFilterableNorSortable() {
        assertThat(AttributeCapabilities.isFilterable(ValueType.TEXT)).isFalse();
        assertThat(AttributeCapabilities.isSortable(ValueType.TEXT)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ValueType.class, names = "TEXT", mode = EnumSource.Mode.EXCLUDE)
    void everyNonTextTypeIsBothFilterableAndSortable(ValueType type) {
        assertThat(AttributeCapabilities.isFilterable(type)).isTrue();
        assertThat(AttributeCapabilities.isSortable(type)).isTrue();
    }

    @Test
    void nullTypeIsNeitherFilterableNorSortable() {
        assertThat(AttributeCapabilities.isFilterable((ValueType) null)).isFalse();
        assertThat(AttributeCapabilities.isSortable((ValueType) null)).isFalse();
    }

    @Test
    void nullAttributeIsNeitherFilterableNorSortable() {
        assertThat(AttributeCapabilities.isFilterable((Attribute) null)).isFalse();
        assertThat(AttributeCapabilities.isSortable((Attribute) null)).isFalse();
    }

    @Test
    void delegatesToAttributeType() {
        Attribute number = new Attribute.Builder().withType(ValueType.NUMBER).build();
        Attribute text = new Attribute.Builder().withType(ValueType.TEXT).build();

        assertThat(AttributeCapabilities.isFilterable(number)).isTrue();
        assertThat(AttributeCapabilities.isSortable(number)).isTrue();
        assertThat(AttributeCapabilities.isFilterable(text)).isFalse();
        assertThat(AttributeCapabilities.isSortable(text)).isFalse();
    }
}
