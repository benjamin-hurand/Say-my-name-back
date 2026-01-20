// src/main/java/com/saymyname/core/model/course/CourseQuestionAttempt.java
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
public class CourseQuestionAttempt {

    private Long id;
    private Course course;

    private int questionRound;

    private LocalDateTime askedAt;
    private LocalDateTime answeredAt;
    private int responseTimeMs;

    private String rawSubmission;
    private String normalizedAudit;

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

    public CourseQuestionAttempt() {
    }

    private CourseQuestionAttempt(Builder b) {
        this.id = b.id;
        this.course = b.course;
        this.questionRound = b.questionRound;
        this.askedAt = b.askedAt;
        this.answeredAt = b.answeredAt;
        this.responseTimeMs = b.responseTimeMs;
        this.rawSubmission = b.rawSubmission;
        this.normalizedAudit = b.normalizedAudit;
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
            throw new IllegalStateException("CourseQuestionAttempt.course is required");
        if (askedAt == null)
            throw new IllegalStateException("CourseQuestionAttempt.askedAt is required");
        if (poolType == null)
            throw new IllegalStateException("CourseQuestionAttempt.poolType is required");
        if (questionRound < 0)
            throw new IllegalStateException("CourseQuestionAttempt.questionRound must be >= 0");
        if (responseTimeMs < 0)
            throw new IllegalStateException("CourseQuestionAttempt.responseTimeMs must be >= 0");

        if (snapshot != null)
            snapshot.validateInvariants();

        if (plan != null)
            plan.validateInvariants();

        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("CourseQuestionAttempt must contain at least 1 item");
        }
        for (CourseQuestionItem item : items) {
            if (item == null)
                throw new IllegalStateException("CourseQuestionAttempt.items cannot contain null");
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

    public String getNormalizedAudit() {
        return normalizedAudit;
    }

    public void setNormalizedAudit(String normalizedAudit) {
        this.normalizedAudit = normalizedAudit;
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
        private String normalizedAudit;
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

        public Builder withNormalizedAudit(String normalizedAudit) {
            this.normalizedAudit = normalizedAudit;
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

        public CourseQuestionAttempt build() {
            return new CourseQuestionAttempt(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CourseQuestionAttempt))
            return false;
        CourseQuestionAttempt that = (CourseQuestionAttempt) o;
        return questionRound == that.questionRound
                && responseTimeMs == that.responseTimeMs
                && globalCorrect == that.globalCorrect
                && helpUsed == that.helpUsed
                && Objects.equals(id, that.id)
                && Objects.equals(course, that.course)
                && Objects.equals(askedAt, that.askedAt)
                && Objects.equals(answeredAt, that.answeredAt)
                && Objects.equals(rawSubmission, that.rawSubmission)
                && Objects.equals(normalizedAudit, that.normalizedAudit)
                && poolType == that.poolType
                && Objects.equals(snapshot, that.snapshot)
                && Objects.equals(plan, that.plan)
                && Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, course, questionRound, askedAt, answeredAt, responseTimeMs,
                rawSubmission, normalizedAudit, globalCorrect, poolType, helpUsed, snapshot, plan, items);
    }
}
