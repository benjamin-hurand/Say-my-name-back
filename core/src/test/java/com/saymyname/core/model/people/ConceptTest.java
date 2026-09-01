package com.saymyname.core.model.people;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConceptTest {

    @Test
    void builderPreservesNullableRequiredMaxValues() {
        Concept unconstrained = new Concept.Builder()
                .withRequiredMaxValues(null)
                .build();
        Concept singleValue = new Concept.Builder()
                .withRequiredMaxValues(1)
                .build();

        assertThat(unconstrained.getRequiredMaxValues()).isNull();
        assertThat(singleValue.getRequiredMaxValues()).isEqualTo(1);
    }
}
