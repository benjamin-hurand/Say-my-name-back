package com.saymyname.core.model.quiz.options;

import com.saymyname.core.model.enums.FollowFilter;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TrainingOptions {
    Long id;
    Long gameModeId;
    FollowFilter populationScope;
    CategorySelection category;
    Boolean initialGiven;
    Boolean trackKnowledge;
}
