package com.saymyname.core.validation;

import com.saymyname.core.exception.common.ValidationException;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.Concept;
import com.saymyname.core.model.people.ValueType;
import com.saymyname.core.model.enums.EditPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttributeDefinitionValidatorTest {

    @ParameterizedTest
    @ValueSource(ints = { 0, -1 })
    void rejectsNonPositiveMaxValues(int maxValues) {
        Attribute attribute = attribute(ValueType.TEXT, maxValues);

        assertThatThrownBy(() -> AttributeDefinitionValidator.validate(attribute, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("maxValues");
    }

    @Test
    void acceptsOneForCustomAttribute() {
        assertThatCode(() -> AttributeDefinitionValidator.validate(attribute(ValueType.TEXT, 1), null))
                .doesNotThrowAnyException();
    }

    @Test
    void enforcesRequiredMaxValuesExactly() {
        Concept concept = concept(ValueType.TEXT, 1);

        assertThatCode(() -> AttributeDefinitionValidator.validate(attribute(ValueType.TEXT, 1), concept))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> AttributeDefinitionValidator.validate(attribute(ValueType.TEXT, 2), concept))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("requiredMaxValues");
    }

    @Test
    void allowsAnyPositiveMaxValuesWhenConceptDoesNotRequireOne() {
        Concept concept = concept(ValueType.TEXT, null);

        assertThatCode(() -> AttributeDefinitionValidator.validate(attribute(ValueType.TEXT, 1), concept))
                .doesNotThrowAnyException();
        assertThatCode(() -> AttributeDefinitionValidator.validate(attribute(ValueType.TEXT, 3), concept))
                .doesNotThrowAnyException();
    }

    @Test
    void enforcesConceptValueType() {
        Concept concept = concept(ValueType.DATE, null);

        assertThatCode(() -> AttributeDefinitionValidator.validate(attribute(ValueType.DATE, 1), concept))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> AttributeDefinitionValidator.validate(attribute(ValueType.TEXT, 1), concept))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("type");
    }

    @Test
    void acceptsEligibleConceptAsIdentitySource() {
        Attribute attribute = attribute(ValueType.TEXT, 1);
        attribute.setIdentitySource(true);
        Concept concept = new Concept.Builder()
                .withCode("FIRST_NAME")
                .withValueType(ValueType.TEXT)
                .withIdentityComponentEligible(true)
                .build();

        assertThatCode(() -> AttributeDefinitionValidator.validate(attribute, concept)).doesNotThrowAnyException();
    }

    @Test
    void rejectsIneligibleConceptAsIdentitySource() {
        Attribute attribute = attribute(ValueType.ENUM, 1);
        attribute.setIdentitySource(true);
        Concept concept = new Concept.Builder()
                .withCode("GENDER")
                .withValueType(ValueType.ENUM)
                .withIdentityComponentEligible(false)
                .build();

        assertThatThrownBy(() -> AttributeDefinitionValidator.validate(attribute, concept))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("source d'identité");
    }

    @Test
    void rejectsCustomAttributeAsIdentitySource() {
        // MVP: custom attributes (no concept) can no longer be identity sources,
        // even a single-value TEXT attribute that was allowed under the old rule.
        Attribute attribute = attribute(ValueType.TEXT, 1);
        attribute.setIdentitySource(true);

        assertThatThrownBy(() -> AttributeDefinitionValidator.validate(attribute, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("source d'identité");
    }

    @Test
    void rejectsEnumAndMultiValueCustomIdentitySources() {
        Attribute enumAttribute = attribute(ValueType.ENUM, 1);
        enumAttribute.setIdentitySource(true);
        Attribute multiAttribute = attribute(ValueType.TEXT, 2);
        multiAttribute.setIdentitySource(true);

        assertThatThrownBy(() -> AttributeDefinitionValidator.validate(enumAttribute, null))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> AttributeDefinitionValidator.validate(multiAttribute, null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void enforcesIdentitySystemConfiguration() {
        Concept identity = new Concept.Builder()
                .withCode("IDENTITY")
                .withValueType(ValueType.TEXT)
                .withRequiredMaxValues(1)
                .build();
        Attribute valid = attribute(ValueType.TEXT, 1);
        valid.setEditPolicy(EditPolicy.DERIVED);

        assertThatCode(() -> AttributeDefinitionValidator.validate(valid, identity)).doesNotThrowAnyException();

        valid.setIdentitySource(true);
        assertThatThrownBy(() -> AttributeDefinitionValidator.validate(valid, identity))
                .isInstanceOf(ValidationException.class);
    }

    private Attribute attribute(ValueType type, int maxValues) {
        return new Attribute.Builder()
                .withType(type)
                .withMaxValues(maxValues)
                .build();
    }

    private Concept concept(ValueType valueType, Integer requiredMaxValues) {
        return new Concept.Builder()
                .withValueType(valueType)
                .withRequiredMaxValues(requiredMaxValues)
                .build();
    }
}
