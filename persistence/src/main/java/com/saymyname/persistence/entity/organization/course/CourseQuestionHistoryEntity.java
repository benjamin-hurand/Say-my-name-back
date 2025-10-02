package com.saymyname.persistence.entity.organization.course;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import com.saymyname.core.model.enums.PoolType;
import com.saymyname.persistence.multitenancy.BaseOrgScoped;

@Entity
@Table(name = "course_question_history")
public class CourseQuestionHistoryEntity extends BaseOrgScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "knowledge_id", nullable = false)
    private KnowledgeEntity knowledge;

    @Column(name = "question_round", nullable = false)
    private int questionRound;

    @Column(name = "asked_at", nullable = false)
    private LocalDateTime askedAt;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    @Column(name = "response_time_ms", nullable = false)
    private int responseTimeMs;

    @Column(name = "user_answer")
    private String userAnswer;

    @Column(name = "correct", nullable = false)
    private boolean correct;

    @Enumerated(EnumType.STRING)
    @Column(name = "pool_type", nullable = false)
    private PoolType poolType;

    @Column(name = "help_used", nullable = false)
    private boolean helpUsed;

    public CourseQuestionHistoryEntity() {
    }

    private CourseQuestionHistoryEntity(Builder b) {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CourseEntity getCourse() {
        return course;
    }

    public void setCourse(CourseEntity course) {
        this.course = course;
    }

    public KnowledgeEntity getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(KnowledgeEntity knowledge) {
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

    public static class Builder {
        private Long id;
        private CourseEntity course;
        private KnowledgeEntity knowledge;
        private int questionRound;
        private LocalDateTime askedAt;
        private LocalDateTime answeredAt;
        private int responseTimeMs;
        private String userAnswer;
        private boolean correct;
        private PoolType poolType;
        private boolean helpUsed;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withCourse(CourseEntity course) {
            this.course = course;
            return this;
        }

        public Builder withKnowledge(KnowledgeEntity knowledge) {
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

        public CourseQuestionHistoryEntity build() {
            return new CourseQuestionHistoryEntity(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CourseQuestionHistoryEntity))
            return false;
        CourseQuestionHistoryEntity that = (CourseQuestionHistoryEntity) o;
        return questionRound == that.questionRound &&
                responseTimeMs == that.responseTimeMs &&
                correct == that.correct &&
                id.equals(that.id) &&
                course.equals(that.course) &&
                knowledge.equals(that.knowledge) &&
                askedAt.equals(that.askedAt) &&
                answeredAt.equals(that.answeredAt) &&
                ((userAnswer == null && that.userAnswer == null)
                        || (userAnswer != null && userAnswer.equals(that.userAnswer)))
                && helpUsed == that.helpUsed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, course, knowledge, questionRound, askedAt, answeredAt, responseTimeMs, userAnswer,
                correct, poolType,
                helpUsed);
    }

    @Override
    public String toString() {
        return "CourseQuestionHistoryEntity{" +
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
