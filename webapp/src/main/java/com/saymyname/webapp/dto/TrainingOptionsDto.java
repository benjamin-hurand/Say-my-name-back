// src/main/java/com/saymyname/webapp/dto/TrainingOptionsDto.java
package com.saymyname.webapp.dto;

import com.saymyname.core.model.enums.FollowFilter;

public record TrainingOptionsDto(
                Long targetAttributeId,
                FollowFilter populationScope,
                TrainingCategorySelectionDto category, // nullable
                Boolean trackKnowledge) {
}
