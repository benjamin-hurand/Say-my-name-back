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

        return TrainingOptions.builder()
                .gameModeId(dto.gameModeId())
                .populationScope(dto.populationScope())
                .category(category)
                .trackKnowledge(dto.trackKnowledge())
                .build();
    }

    private static CategorySelection toCategoryModel(TrainingCategorySelectionDto dto) {
        if (dto == null) {
            return null;
        }
        return CategorySelection.builder()
                .attributeId(dto.attributeId())
                .value(dto.value())
                .build();
    }
}
