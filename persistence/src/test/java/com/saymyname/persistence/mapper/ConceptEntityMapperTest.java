package com.saymyname.persistence.mapper;

import com.saymyname.core.model.people.Concept;
import com.saymyname.core.model.people.ValueType;
import com.saymyname.persistence.entity.concept.ConceptEntity;
import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConceptEntityMapperTest {

    private final ConceptEntityMapper mapper = new ConceptEntityMapper();

    @Test
    void mapsNullRequiredMaxValuesBothWays() {
        ConceptEntity entity = ConceptEntity.builder()
                .valueType(ValueType.TEXT)
                .requiredMaxValues(null)
                .build();

        Concept model = mapper.toModel(entity);

        assertThat(model.getRequiredMaxValues()).isNull();
        assertThat(mapper.toEntity(model).getRequiredMaxValues()).isNull();
    }

    @Test
    void mapsRequiredMaxValuesOneBothWays() {
        Concept model = new Concept.Builder()
                .withValueType(ValueType.TEXT)
                .withRequiredMaxValues(1)
                .build();

        ConceptEntity entity = mapper.toEntity(model);

        assertThat(entity.getRequiredMaxValues()).isEqualTo(1);
        assertThat(mapper.toModel(entity).getRequiredMaxValues()).isEqualTo(1);
    }

    @Test
    void mapsRequiredMaxValuesToExpectedColumn() throws NoSuchFieldException {
        Column column = ConceptEntity.class.getDeclaredField("requiredMaxValues").getAnnotation(Column.class);

        assertThat(column).isNotNull();
        assertThat(column.name()).isEqualTo("required_max_values");
    }
}
