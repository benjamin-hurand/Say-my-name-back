// src/main/java/com/saymyname/core/model/quiz/QuizQuestionSpec.java
package com.saymyname.core.model.quiz;

import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;

public class QuizQuestionSpec {

    private QuizQuestionSource source;

    private Long personId;
    private String storageKey;

    private Long targetAttributeId;

    private QuizQuestionContext context;

    private String initials; // optional

    // ---- plugin helpers (core-friendly) ----
    private List<Long> candidatePoolPersonIds; // optional but strongly recommended for MCQ/association/ordering
    private List<QuizPayloadItem> candidatePoolItems; // optional, includes storageKey for media
    private Boolean timed; // explicit timing toggle
    private Integer timeLimitMs; // required when timed=true
    private Integer maxErrorsOverride; // optional (for HANGMAN)
    private QuizDecisionReasonCode reasonCode;
    private String reasonDetailsJson;

    public QuizQuestionSpec() {
    }

    private QuizQuestionSpec(Builder b) {
        this.source = b.source;
        this.personId = b.personId;
        this.storageKey = b.storageKey;

        this.targetAttributeId = b.targetAttributeId;

        this.context = b.context;
        this.initials = b.initials;
        this.candidatePoolPersonIds = b.candidatePoolPersonIds;
        this.candidatePoolItems = b.candidatePoolItems;
        this.timed = b.timed;
        this.timeLimitMs = b.timeLimitMs;
        this.maxErrorsOverride = b.maxErrorsOverride;
        this.reasonCode = b.reasonCode;
        this.reasonDetailsJson = b.reasonDetailsJson;
    }

    public QuizQuestionSource getSource() {
        return source;
    }

    public Long getPersonId() {
        return personId;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public Long getTargetAttributeId() {
        return targetAttributeId;
    }

    public QuizQuestionContext getContext() {
        return context;
    }

    public String getInitials() {
        return initials;
    }

    public List<Long> getCandidatePoolPersonIds() {
        return candidatePoolPersonIds;
    }

    public List<QuizPayloadItem> getCandidatePoolItems() {
        return candidatePoolItems;
    }

    public Boolean getTimed() {
        return timed;
    }

    public Integer getTimeLimitMs() {
        return timeLimitMs;
    }

    public Integer getMaxErrorsOverride() {
        return maxErrorsOverride;
    }

    public QuizDecisionReasonCode getReasonCode() {
        return reasonCode;
    }

    public String getReasonDetailsJson() {
        return reasonDetailsJson;
    }

    public void setSource(QuizQuestionSource source) {
        this.source = source;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public void setTargetAttributeId(Long targetAttributeId) {
        this.targetAttributeId = targetAttributeId;
    }

    public void setContext(QuizQuestionContext context) {
        this.context = context;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public void setCandidatePoolPersonIds(List<Long> candidatePoolPersonIds) {
        this.candidatePoolPersonIds = candidatePoolPersonIds;
    }

    public void setCandidatePoolItems(List<QuizPayloadItem> candidatePoolItems) {
        this.candidatePoolItems = candidatePoolItems;
    }

    public void setTimed(Boolean timed) {
        this.timed = timed;
    }

    public void setTimeLimitMs(Integer timeLimitMs) {
        this.timeLimitMs = timeLimitMs;
    }

    public void setMaxErrorsOverride(Integer maxErrorsOverride) {
        this.maxErrorsOverride = maxErrorsOverride;
    }

    public void setReasonCode(QuizDecisionReasonCode reasonCode) {
        this.reasonCode = reasonCode;
    }

    public void setReasonDetailsJson(String reasonDetailsJson) {
        this.reasonDetailsJson = reasonDetailsJson;
    }

    public static class Builder {
        private QuizQuestionSource source;
        private Long personId;
        private String storageKey;
        private Long targetAttributeId;

        private QuizQuestionContext context;

        private String initials;
        private List<Long> candidatePoolPersonIds;
        private List<QuizPayloadItem> candidatePoolItems;
        private Boolean timed;
        private Integer timeLimitMs;
        private Integer maxErrorsOverride;
        private QuizDecisionReasonCode reasonCode;
        private String reasonDetailsJson;

        public Builder withSource(QuizQuestionSource v) {
            this.source = v;
            return this;
        }

        public Builder withPersonId(Long v) {
            this.personId = v;
            return this;
        }

        public Builder withStorageKey(String v) {
            this.storageKey = v;
            return this;
        }

        public Builder withTargetAttributeId(Long v) {
            this.targetAttributeId = v;
            return this;
        }

        public Builder withContext(QuizQuestionContext v) {
            this.context = v;
            return this;
        }

        public Builder withInitials(String v) {
            this.initials = v;
            return this;
        }

        public Builder withCandidatePoolPersonIds(List<Long> v) {
            this.candidatePoolPersonIds = v;
            return this;
        }

        public Builder withCandidatePoolItems(List<QuizPayloadItem> v) {
            this.candidatePoolItems = v;
            return this;
        }

        public Builder withTimed(Boolean v) {
            this.timed = v;
            return this;
        }

        public Builder withTimeLimitMs(Integer v) {
            this.timeLimitMs = v;
            return this;
        }

        public Builder withMaxErrorsOverride(Integer v) {
            this.maxErrorsOverride = v;
            return this;
        }

        public Builder withReasonCode(QuizDecisionReasonCode v) {
            this.reasonCode = v;
            return this;
        }

        public Builder withReasonDetailsJson(String v) {
            this.reasonDetailsJson = v;
            return this;
        }

        public QuizQuestionSpec build() {
            Objects.requireNonNull(source, "source");
            Objects.requireNonNull(personId, "personId");
            Objects.requireNonNull(targetAttributeId, "targetAttributeId");
            Objects.requireNonNull(context, "context");

            return new QuizQuestionSpec(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizQuestionSpec))
            return false;
        QuizQuestionSpec that = (QuizQuestionSpec) o;
        return source == that.source
                && Objects.equals(personId, that.personId)
                && Objects.equals(storageKey, that.storageKey)
                && Objects.equals(targetAttributeId, that.targetAttributeId)
                && Objects.equals(context, that.context)
                && Objects.equals(initials, that.initials)
                && Objects.equals(candidatePoolPersonIds, that.candidatePoolPersonIds)
                && Objects.equals(candidatePoolItems, that.candidatePoolItems)
                && Objects.equals(timed, that.timed)
                && Objects.equals(timeLimitMs, that.timeLimitMs)
                && Objects.equals(maxErrorsOverride, that.maxErrorsOverride)
                && Objects.equals(reasonCode, that.reasonCode)
                && Objects.equals(reasonDetailsJson, that.reasonDetailsJson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                source,
                personId,
                storageKey,
                targetAttributeId,
                context,
                initials,
                candidatePoolPersonIds,
                candidatePoolItems,
                timed,
                timeLimitMs,
                maxErrorsOverride,
                reasonCode,
                reasonDetailsJson);
    }

    @Override
    public String toString() {
        return "QuizQuestionSpec{" +
                "source=" + source +
                ", personId=" + personId +
                ", storageKey='" + storageKey + '\'' +
                ", targetAttributeId=" + targetAttributeId +
                ", context=" + context +
                ", initials='" + initials + '\'' +
                ", candidatePoolPersonIds=" + candidatePoolPersonIds +
                ", candidatePoolItems=" + candidatePoolItems +
                ", timed=" + timed +
                ", timeLimitMs=" + timeLimitMs +
                ", maxErrorsOverride=" + maxErrorsOverride +
                ", reasonCode='" + reasonCode + '\'' +
                ", reasonDetailsJson='" + reasonDetailsJson + '\'' +
                '}';
    }
}