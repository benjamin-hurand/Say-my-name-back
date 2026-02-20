package com.saymyname.core.model.quiz.candidate;

import com.saymyname.core.model.enums.FollowFilter;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CandidateQuery {
    Long userId;
    Long excludePersonId;
    @Builder.Default
    FollowFilter populationScope = FollowFilter.ALL;
    Long categoryAttributeId;
    String categoryValue;
    boolean requireApprovedPhoto;
    boolean requireCategoryMatch;
    @Builder.Default
    Integer limit = 200;
    Long seed;
    Long gameModeId;
    @Builder.Default
    List<Long> gameModeAttributeIds = List.of();
    String attributeOperator;
    boolean countOnly;

    public static class CandidateQueryBuilder {
        public CandidateQueryBuilder category(Long categoryAttributeId, String categoryValue) {
            this.categoryAttributeId = categoryAttributeId;
            this.categoryValue = categoryValue;
            return this;
        }
    }
}
