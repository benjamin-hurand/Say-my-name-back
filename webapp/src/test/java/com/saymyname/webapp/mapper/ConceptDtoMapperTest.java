package com.saymyname.webapp.mapper;

import com.saymyname.core.model.people.Concept;
import com.saymyname.webapp.dto.ConceptDto;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ConceptDtoMapperTest {

    private final ConceptDtoMapper mapper = new ConceptDtoMapper();

    @Test
    void mapsNullableRequiredMaxValues() {
        Concept unconstrained = new Concept.Builder().withRequiredMaxValues(null).build();
        Concept singleValue = new Concept.Builder().withRequiredMaxValues(1).build();

        assertThat(mapper.toDto(unconstrained).requiredMaxValues()).isNull();
        assertThat(mapper.toDto(singleValue).requiredMaxValues()).isEqualTo(1);
    }

    @Test
    void conceptContractContainsOnlyCurrentComponents() {
        assertThat(Arrays.stream(ConceptDto.class.getRecordComponents())
                .map(component -> component.getName()))
                .containsExactly(
                        "id",
                        "code",
                        "iconKey",
                        "valueType",
                        "derived",
                        "portabilityKind",
                        "identityComponentEligible",
                        "requiredMaxValues",
                        "defaultCasingStrategy");
    }
}
