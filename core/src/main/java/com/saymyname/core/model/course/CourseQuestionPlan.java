package com.saymyname.core.model.course;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.enums.quiz.QuizFormat;

public class CourseQuestionPlan {

    private QuizFormat format;
    private boolean timed;
    private Integer timeLimitMs;
    private int targetCount;
    private List<Long> targetKnowledgeIds = new ArrayList<>();
    private String paramsJson;
    private String reason;

    public CourseQuestionPlan() {
    }

    private CourseQuestionPlan(Builder b) {
        this.format = b.format;
        this.timed = b.timed;
        this.timeLimitMs = b.timeLimitMs;
        this.targetCount = b.targetCount;
        this.targetKnowledgeIds = b.targetKnowledgeIds != null ? b.targetKnowledgeIds : new ArrayList<>();
        this.paramsJson = b.paramsJson;
        this.reason = b.reason;
        validateInvariants();
    }

    public void validateInvariants() {
        if (format == null)
            throw new IllegalStateException("CourseQuestionPlan.format is required");
        if (targetCount < 1)
            throw new IllegalStateException("CourseQuestionPlan.targetCount must be >= 1");
        if (timed && (timeLimitMs == null || timeLimitMs < 1000)) {
            throw new IllegalStateException("CourseQuestionPlan.timeLimitMs must be >= 1000 when timed=true");
        }
        if (targetKnowledgeIds == null || targetKnowledgeIds.isEmpty()) {
            throw new IllegalStateException("CourseQuestionPlan.targetKnowledgeIds must contain at least 1 id");
        }
        if (targetKnowledgeIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("CourseQuestionPlan.targetKnowledgeIds cannot contain null");
        }
        if (targetKnowledgeIds.size() != targetCount) {
            throw new IllegalStateException("CourseQuestionPlan.targetCount must match targetKnowledgeIds size");
        }
    }

    public QuizFormat getFormat() {
        return format;
    }

    public boolean isTimed() {
        return timed;
    }

    public Integer getTimeLimitMs() {
        return timeLimitMs;
    }

    public int getTargetCount() {
        return targetCount;
    }

    public List<Long> getTargetKnowledgeIds() {
        return targetKnowledgeIds;
    }

    public String getParamsJson() {
        return paramsJson;
    }

    public String getReason() {
        return reason;
    }

    public void setFormat(QuizFormat format) {
        this.format = format;
    }

    public void setTimed(boolean timed) {
        this.timed = timed;
    }

    public void setTimeLimitMs(Integer timeLimitMs) {
        this.timeLimitMs = timeLimitMs;
    }

    public void setTargetCount(int targetCount) {
        this.targetCount = targetCount;
    }

    public void setTargetKnowledgeIds(List<Long> targetKnowledgeIds) {
        this.targetKnowledgeIds = targetKnowledgeIds;
    }

    public void setParamsJson(String paramsJson) {
        this.paramsJson = paramsJson;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public static class Builder {
        private QuizFormat format;
        private boolean timed;
        private Integer timeLimitMs;
        private int targetCount;
        private List<Long> targetKnowledgeIds;
        private String paramsJson;
        private String reason;

        public Builder withFormat(QuizFormat format) {
            this.format = format;
            return this;
        }

        public Builder withTimed(boolean timed) {
            this.timed = timed;
            return this;
        }

        public Builder withTimeLimitMs(Integer timeLimitMs) {
            this.timeLimitMs = timeLimitMs;
            return this;
        }

        public Builder withTargetCount(int targetCount) {
            this.targetCount = targetCount;
            return this;
        }

        public Builder withTargetKnowledgeIds(List<Long> targetKnowledgeIds) {
            this.targetKnowledgeIds = targetKnowledgeIds;
            return this;
        }

        public Builder withParamsJson(String paramsJson) {
            this.paramsJson = paramsJson;
            return this;
        }

        public Builder withReason(String reason) {
            this.reason = reason;
            return this;
        }

        public CourseQuestionPlan build() {
            return new CourseQuestionPlan(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CourseQuestionPlan))
            return false;
        CourseQuestionPlan that = (CourseQuestionPlan) o;
        return timed == that.timed
                && targetCount == that.targetCount
                && format == that.format
                && Objects.equals(timeLimitMs, that.timeLimitMs)
                && Objects.equals(targetKnowledgeIds, that.targetKnowledgeIds)
                && Objects.equals(paramsJson, that.paramsJson)
                && Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(format, timed, timeLimitMs, targetCount, targetKnowledgeIds, paramsJson, reason);
    }
}
