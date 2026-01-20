// src/main/java/com/saymyname/core/model/quiz/QuizQuestionContext.java
package com.saymyname.core.model.quiz;

import java.util.Objects;

import com.saymyname.core.model.enums.DifficultyLevel;
import com.saymyname.core.model.enums.PoolType;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;

public class QuizQuestionContext {

    private QuizQuestionSource source;

    // COURSE context
    private Long courseId;
    private Long courseQuestionId;
    private Integer questionRound;
    private PoolType poolType;
    private DifficultyLevel difficultyLevel;

    // TRAINING context
    private Long reducedOptionsId;
    private Boolean knowledgeTracking;

    public QuizQuestionContext() {
    }

    private QuizQuestionContext(Builder b) {
        this.source = b.source;
        this.courseId = b.courseId;
        this.courseQuestionId = b.courseQuestionId;
        this.questionRound = b.questionRound;
        this.poolType = b.poolType;
        this.difficultyLevel = b.difficultyLevel;
        this.reducedOptionsId = b.reducedOptionsId;
        this.knowledgeTracking = b.knowledgeTracking;
    }

    public QuizQuestionSource getSource() {
        return source;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getCourseQuestionId() {
        return courseQuestionId;
    }

    public Integer getQuestionRound() {
        return questionRound;
    }

    public PoolType getPoolType() {
        return poolType;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public Long getReducedOptionsId() {
        return reducedOptionsId;
    }

    public Boolean getKnowledgeTracking() {
        return knowledgeTracking;
    }

    public void setSource(QuizQuestionSource source) {
        this.source = source;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public void setCourseQuestionId(Long courseQuestionId) {
        this.courseQuestionId = courseQuestionId;
    }

    public void setQuestionRound(Integer questionRound) {
        this.questionRound = questionRound;
    }

    public void setPoolType(PoolType poolType) {
        this.poolType = poolType;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public void setReducedOptionsId(Long reducedOptionsId) {
        this.reducedOptionsId = reducedOptionsId;
    }

    public void setKnowledgeTracking(Boolean knowledgeTracking) {
        this.knowledgeTracking = knowledgeTracking;
    }

    public static class Builder {
        private QuizQuestionSource source;

        private Long courseId;
        private Long courseQuestionId;
        private Integer questionRound;
        private PoolType poolType;
        private DifficultyLevel difficultyLevel;

        private Long reducedOptionsId;
        private Boolean knowledgeTracking;

        public Builder withSource(QuizQuestionSource v) {
            this.source = v;
            return this;
        }

        public Builder withCourseId(Long v) {
            this.courseId = v;
            return this;
        }

        public Builder withCourseQuestionId(Long v) {
            this.courseQuestionId = v;
            return this;
        }

        public Builder withQuestionRound(Integer v) {
            this.questionRound = v;
            return this;
        }

        public Builder withPoolType(PoolType v) {
            this.poolType = v;
            return this;
        }

        public Builder withDifficultyLevel(DifficultyLevel v) {
            this.difficultyLevel = v;
            return this;
        }

        public Builder withReducedOptionsId(Long v) {
            this.reducedOptionsId = v;
            return this;
        }

        public Builder withKnowledgeTracking(Boolean v) {
            this.knowledgeTracking = v;
            return this;
        }

        public QuizQuestionContext build() {
            return new QuizQuestionContext(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizQuestionContext))
            return false;
        QuizQuestionContext that = (QuizQuestionContext) o;
        return source == that.source
                && Objects.equals(courseId, that.courseId)
                && Objects.equals(courseQuestionId, that.courseQuestionId)
                && Objects.equals(questionRound, that.questionRound)
                && poolType == that.poolType
                && difficultyLevel == that.difficultyLevel
                && Objects.equals(reducedOptionsId, that.reducedOptionsId)
                && Objects.equals(knowledgeTracking, that.knowledgeTracking);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, courseId, courseQuestionId, questionRound, poolType, difficultyLevel,
                reducedOptionsId, knowledgeTracking);
    }

    @Override
    public String toString() {
        return "QuizQuestionContext{" +
                "source=" + source +
                ", courseId=" + courseId +
                ", courseQuestionId=" + courseQuestionId +
                ", questionRound=" + questionRound +
                ", poolType=" + poolType +
                ", difficultyLevel=" + difficultyLevel +
                ", reducedOptionsId=" + reducedOptionsId +
                ", knowledgeTracking=" + knowledgeTracking +
                '}';
    }
}
