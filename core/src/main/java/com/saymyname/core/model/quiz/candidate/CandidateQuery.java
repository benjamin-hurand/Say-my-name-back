// core/model/quiz/candidate/CandidateQuery.java
package com.saymyname.core.model.quiz.candidate;

import java.util.Objects;

import com.saymyname.core.model.enums.FollowFilter;

public class CandidateQuery {

    private Long userId; // requis si FOLLOWED/UNFOLLOWED
    private Long excludePersonId; // self
    private FollowFilter populationScope; // ALL/FOLLOWED/UNFOLLOWED

    private Long categoryAttributeId; // nullable
    private String categoryValue; // nullable

    private boolean requireApprovedPhoto; // pour éviter de faire échouer des formats photo-based
    private boolean requireCategoryMatch; // si category* est set, enforce

    private Integer limit; // pool size
    private Long seed; // optionnel si tu veux random stable

    /**
     * Attribute constraints for candidate eligibility.
     * Interpreted as "person must have this attributeId"
     * (i.e., presence constraint), typically used for IDENTITY or a single target
     * attribute.
     */
    private Long attributeId; // nullable/empty => no attribute eligibility constraint

    private boolean countOnly; // hint optimisation (pas d'ORDER BY)

    public Long getUserId() {
        return userId;
    }

    public Long getExcludePersonId() {
        return excludePersonId;
    }

    public FollowFilter getPopulationScope() {
        return populationScope;
    }

    public Long getCategoryAttributeId() {
        return categoryAttributeId;
    }

    public String getCategoryValue() {
        return categoryValue;
    }

    public boolean isRequireApprovedPhoto() {
        return requireApprovedPhoto;
    }

    public boolean isRequireCategoryMatch() {
        return requireCategoryMatch;
    }

    public Integer getLimit() {
        return limit;
    }

    public Long getSeed() {
        return seed;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public boolean isCountOnly() {
        return countOnly;
    }

    public static class Builder {
        private final CandidateQuery q = new CandidateQuery();

        public Builder withUserId(Long v) {
            q.userId = v;
            return this;
        }

        public Builder withExcludePersonId(Long v) {
            q.excludePersonId = v;
            return this;
        }

        public Builder withPopulationScope(FollowFilter v) {
            q.populationScope = v;
            return this;
        }

        public Builder withCategory(Long attrId, String value) {
            q.categoryAttributeId = attrId;
            q.categoryValue = value;
            return this;
        }

        public Builder requireApprovedPhoto(boolean v) {
            q.requireApprovedPhoto = v;
            return this;
        }

        public Builder requireCategoryMatch(boolean v) {
            q.requireCategoryMatch = v;
            return this;
        }

        public Builder withLimit(Integer v) {
            q.limit = v;
            return this;
        }

        public Builder withSeed(Long v) {
            q.seed = v;
            return this;
        }

        public Builder withAttributeId(Long v) {
            q.attributeId = v;
            return this;
        }

        public Builder countOnly(boolean v) {
            q.countOnly = v;
            return this;
        }

        public CandidateQuery build() {
            // validations minimales
            if (q.populationScope == null) {
                q.populationScope = FollowFilter.ALL;
            }
            if ((q.populationScope == FollowFilter.FOLLOWED || q.populationScope == FollowFilter.UNFOLLOWED)
                    && q.userId == null) {
                throw new IllegalArgumentException("userId is required for FOLLOWED/UNFOLLOWED");
            }
            if (q.categoryAttributeId != null) {
                Objects.requireNonNull(q.categoryValue, "categoryValue");
            }
            if (q.limit == null || q.limit <= 0) {
                q.limit = 200;
            }

            return q;
        }
    }
}