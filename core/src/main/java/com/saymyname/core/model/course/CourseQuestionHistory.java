package com.saymyname.core.model.course;

import com.saymyname.core.model.enums.PoolType;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Historique d'une question posée dans un cours.
 * Contient uniquement les données d'interaction (asked/answered) et de pool.
 */
public class CourseQuestionHistory {
    private long id;
    private Course course;
    private Knowledge knowledge;
    private int questionRound;
    private LocalDateTime askedAt;
    private LocalDateTime answeredAt;
    private int responseTimeMs;
    private String userAnswer;
    private boolean correct;
    private PoolType poolType;
    private boolean helpUsed;

    public CourseQuestionHistory() {
    }

    private CourseQuestionHistory(Builder b) {
        this.id = b.id;
        this.course = b.course;
        this.knowledge = b.knowledge;
        this.questionRound = b.questionRound;
        this.askedAt = b.askedAt;
        this.answeredAt = b.answeredAt;
        this.responseTimeMs = b.responseTimeMs;
        this.userAnswer = b.userAnswer;
        this.correct = b.correct;
        this.poolType = b.poolType;
        this.helpUsed = b.helpUsed;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Knowledge getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(Knowledge knowledge) {
        this.knowledge = knowledge;
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

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
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

    // Builder pattern
    public static class Builder {
        private long id;
        private Course course;
        private Knowledge knowledge;
        private int questionRound;
        private LocalDateTime askedAt;
        private LocalDateTime answeredAt;
        private int responseTimeMs;
        private String userAnswer;
        private boolean correct;
        private PoolType poolType;
        private boolean helpUsed;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withCourse(Course course) {
            this.course = course;
            return this;
        }

        public Builder withKnowledge(Knowledge knowledge) {
            this.knowledge = knowledge;
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

        public Builder withUserAnswer(String userAnswer) {
            this.userAnswer = userAnswer;
            return this;
        }

        public Builder withCorrect(boolean correct) {
            this.correct = correct;
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
        return id == that.id &&
                questionRound == that.questionRound &&
                responseTimeMs == that.responseTimeMs &&
                correct == that.correct &&
                helpUsed == that.helpUsed &&
                Objects.equals(course, that.course) &&
                Objects.equals(knowledge, that.knowledge) &&
                Objects.equals(askedAt, that.askedAt) &&
                Objects.equals(answeredAt, that.answeredAt) &&
                Objects.equals(userAnswer, that.userAnswer) &&
                poolType == that.poolType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, course, knowledge, questionRound,
                askedAt, answeredAt, responseTimeMs,
                userAnswer, correct, poolType, helpUsed);
    }

    @Override
    public String toString() {
        return "CourseQuestionHistory{" +
                "id=" + id +
                ", questionRound=" + questionRound +
                ", askedAt=" + askedAt +
                ", answeredAt=" + answeredAt +
                ", responseTimeMs=" + responseTimeMs +
                ", userAnswer='" + userAnswer + '\'' +
                ", correct=" + correct +
                ", poolType=" + poolType +
                ", helpUsed=" + helpUsed +
                '}';
    }
}
