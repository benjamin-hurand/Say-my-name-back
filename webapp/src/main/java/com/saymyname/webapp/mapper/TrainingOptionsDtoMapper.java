// src/main/java/com/saymyname/webapp/mapper/TrainingOptionsDtoMapper.java
package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.quiz.options.CategorySelection;
import com.saymyname.core.model.quiz.options.TrainingOptions;
import com.saymyname.webapp.dto.TrainingCategorySelectionDto;
import com.saymyname.webapp.dto.TrainingOptionsDto;

@Component
public class TrainingOptionsDtoMapper {

    public TrainingOptions toModel(TrainingOptionsDto dto) {
        if (dto == null) {
            return null;
        }

        CategorySelection category = toCategoryModel(dto.category());

        return new TrainingOptions.Builder()
                .withGameModeId(dto.gameModeId())
                .withPopulationScope(dto.populationScope())
                .withCategory(category)
                .withTrackKnowledge(dto.trackKnowledge())
                .build();
    }

    private static CategorySelection toCategoryModel(TrainingCategorySelectionDto dto) {
        if (dto == null) {
            return null;
        }
        return new CategorySelection.Builder()
                .withAttributeId(dto.attributeId())
                .withValue(dto.value())
                .build();
    }
}
