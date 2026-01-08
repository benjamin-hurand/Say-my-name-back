// src/main/java/com/saymyname/core/model/course/CourseQuestionHistory.java
package com.saymyname.core.model.course;

import com.saymyname.core.model.enums.PoolType;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Historique d'une question posée dans un cours (event root).
 * Contient :
 * - données d'interaction globales
 * - snapshot immuable de la question posée (Option B)
 * - liste d'items (candidates targets/distractors) pour pool tracking
 */
public class CourseQuestionHistory {

    private Long id;
    private Course course;

    private int questionRound;

    private LocalDateTime askedAt;
    private LocalDateTime answeredAt;
    private int responseTimeMs;

    private String rawSubmission;
    private String normalizedSubmission;

    private boolean globalCorrect;

    private PoolType poolType;
    private boolean helpUsed;

    /**
     * Snapshot complet et immuable de la question posée (affichage + payload +
     * truth).
     * Source de vérité pour audit/replay/revalidation.
     */
    private QuizQuestionSnapshot snapshot;

    /**
     * Plan fige (format/params/timing/targets) pour garantir la coherence.
     */
    private CourseQuestionPlan plan;

    /**
     * Candidates (targets/distractors) pour pool tracking et analytics.
     */
    private List<CourseQuestionItem> items = new ArrayList<>();

    public CourseQuestionHistory() {
    }

    private CourseQuestionHistory(Builder b) {
        this.id = b.id;
        this.course = b.course;
        this.questionRound = b.questionRound;
        this.askedAt = b.askedAt;
        this.answeredAt = b.answeredAt;
        this.responseTimeMs = b.responseTimeMs;
        this.rawSubmission = b.rawSubmission;
        this.normalizedSubmission = b.normalizedSubmission;
        this.globalCorrect = b.globalCorrect;
        this.poolType = b.poolType;
        this.helpUsed = b.helpUsed;
        this.snapshot = b.snapshot;
        this.plan = b.plan;
        this.items = b.items != null ? b.items : new ArrayList<>();
        validateInvariants();
    }

    public void validateInvariants() {
        if (course == null)
            throw new IllegalStateException("CourseQuestionHistory.course is required");
        if (askedAt == null)
            throw new IllegalStateException("CourseQuestionHistory.askedAt is required");
        if (poolType == null)
            throw new IllegalStateException("CourseQuestionHistory.poolType is required");
        if (questionRound < 0)
            throw new IllegalStateException("CourseQuestionHistory.questionRound must be >= 0");
        if (responseTimeMs < 0)
            throw new IllegalStateException("CourseQuestionHistory.responseTimeMs must be >= 0");

        if (snapshot != null)
            snapshot.validateInvariants();

        if (plan != null)
            plan.validateInvariants();

        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("CourseQuestionHistory must contain at least 1 item");
        }
        for (CourseQuestionItem item : items) {
            if (item == null)
                throw new IllegalStateException("CourseQuestionHistory.items cannot contain null");
            item.validateInvariants();
        }
    }

    // Getters/Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public int getQuestionRound() {
        return questionRound;
    }

    public void setQuestionRound(int questionRound) {
        this.questionRound = questionRound;
    }

    public LocalDateTime getAskedAt() {
        return askedAt;
    }

    public void setAskedAt(LocalDateTime askedAt) {
        this.askedAt = askedAt;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }

    public int getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(int responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getRawSubmission() {
        return rawSubmission;
    }

    public void setRawSubmission(String rawSubmission) {
        this.rawSubmission = rawSubmission;
    }

    public String getNormalizedSubmission() {
        return normalizedSubmission;
    }

    public void setNormalizedSubmission(String normalizedSubmission) {
        this.normalizedSubmission = normalizedSubmission;
    }

    public boolean isGlobalCorrect() {
        return globalCorrect;
    }

    public void setGlobalCorrect(boolean globalCorrect) {
        this.globalCorrect = globalCorrect;
    }

    public PoolType getPoolType() {
        return poolType;
    }

    public void setPoolType(PoolType poolType) {
        this.poolType = poolType;
    }

    public boolean isHelpUsed() {
        return helpUsed;
    }

    public void setHelpUsed(boolean helpUsed) {
        this.helpUsed = helpUsed;
    }

    public QuizQuestionSnapshot getSnapshot() {
        return snapshot;
    }

    public CourseQuestionPlan getPlan() {
        return plan;
    }

    public void setSnapshot(QuizQuestionSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public void setPlan(CourseQuestionPlan plan) {
        this.plan = plan;
    }

    public List<CourseQuestionItem> getItems() {
        return items;
    }

    public void setItems(List<CourseQuestionItem> items) {
        this.items = items;
    }

    public static class Builder {
        private Long id;
        private Course course;
        private int questionRound;
        private LocalDateTime askedAt;
        private LocalDateTime answeredAt;
        private int responseTimeMs;
        private String rawSubmission;
        private String normalizedSubmission;
        private boolean globalCorrect;
        private PoolType poolType;
        private boolean helpUsed;
        private QuizQuestionSnapshot snapshot;
        private CourseQuestionPlan plan;
        private List<CourseQuestionItem> items = new ArrayList<>();

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withCourse(Course course) {
            this.course = course;
            return this;
        }

        public Builder withQuestionRound(int questionRound) {
            this.questionRound = questionRound;
            return this;
        }

        public Builder withAskedAt(LocalDateTime askedAt) {
            this.askedAt = askedAt;
            return this;
        }

        public Builder withAnsweredAt(LocalDateTime answeredAt) {
            this.answeredAt = answeredAt;
            return this;
        }

        public Builder withResponseTimeMs(int responseTimeMs) {
            this.responseTimeMs = responseTimeMs;
            return this;
        }

        public Builder withRawSubmission(String rawSubmission) {
            this.rawSubmission = rawSubmission;
            return this;
        }

        public Builder withNormalizedSubmission(String normalizedSubmission) {
            this.normalizedSubmission = normalizedSubmission;
            return this;
        }

        public Builder withGlobalCorrect(boolean globalCorrect) {
            this.globalCorrect = globalCorrect;
            return this;
        }

        public Builder withPoolType(PoolType poolType) {
            this.poolType = poolType;
            return this;
        }

        public Builder withHelpUsed(boolean helpUsed) {
            this.helpUsed = helpUsed;
            return this;
        }

        public Builder withSnapshot(QuizQuestionSnapshot snapshot) {
            this.snapshot = snapshot;
            return this;
        }

        public Builder withPlan(CourseQuestionPlan plan) {
            this.plan = plan;
            return this;
        }

        public Builder withItems(List<CourseQuestionItem> items) {
            this.items = items;
            return this;
        }

        public CourseQuestionHistory build() {
            return new CourseQuestionHistory(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CourseQuestionHistory))
            return false;
        CourseQuestionHistory that = (CourseQuestionHistory) o;
        return questionRound == that.questionRound
                && responseTimeMs == that.responseTimeMs
                && globalCorrect == that.globalCorrect
                && helpUsed == that.helpUsed
                && Objects.equals(id, that.id)
                && Objects.equals(course, that.course)
                && Objects.equals(askedAt, that.askedAt)
                && Objects.equals(answeredAt, that.answeredAt)
                && Objects.equals(rawSubmission, that.rawSubmission)
                && Objects.equals(normalizedSubmission, that.normalizedSubmission)
                && poolType == that.poolType
                && Objects.equals(snapshot, that.snapshot)
                && Objects.equals(plan, that.plan)
                && Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, course, questionRound, askedAt, answeredAt, responseTimeMs,
                rawSubmission, normalizedSubmission, globalCorrect, poolType, helpUsed, snapshot, plan, items);
    }
}
