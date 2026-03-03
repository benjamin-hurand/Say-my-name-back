// src/main/java/com/saymyname/core/model/course/KnowledgeResultEvent.java
package com.saymyname.core.model.course;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Event "SRS / Knowledge update" produced by a question result.
 *
 * Target invariant:
 * - Knowledge tracks a single Fact (atomic).
 *
 * Resolution:
 * - prefer knowledgeId (fast path)
 * - fallback to factId
 * - no fallback on (attributeId, personId) (ambiguous with multi-values)
 */
public class KnowledgeResultEvent {

        private final Long knowledgeId; // optional but preferred
        private final Long factId; // required if knowledgeId is null

        private final boolean correct;
        private final boolean helpUsed;

        // Optional context (analytics/debug)
        private final Long courseId;
        private final Long courseQuestionAttemptId;
        private final Integer questionRound;
        private final LocalDateTime occurredAt;

        private KnowledgeResultEvent(Builder b) {
                this.knowledgeId = b.knowledgeId;
                this.factId = b.factId;
                this.correct = b.correct;
                this.helpUsed = b.helpUsed;

                this.courseId = b.courseId;
                this.courseQuestionAttemptId = b.courseQuestionAttemptId;
                this.questionRound = b.questionRound;
                this.occurredAt = (b.occurredAt != null) ? b.occurredAt : LocalDateTime.now();

                // Minimal validation:
                // We must be able to resolve exactly one target (Knowledge or Fact).
                if (this.knowledgeId == null && this.factId == null) {
                        throw new IllegalArgumentException(
                                        "knowledgeId and factId are both null => cannot resolve target");
                }
        }

        public Long getKnowledgeId() {
                return knowledgeId;
        }

        public Long getFactId() {
                return factId;
        }

        public boolean isCorrect() {
                return correct;
        }

        public boolean isHelpUsed() {
                return helpUsed;
        }

        public Long getCourseId() {
                return courseId;
        }

        public Long getCourseQuestionAttemptId() {
                return courseQuestionAttemptId;
        }

        public Integer getQuestionRound() {
                return questionRound;
        }

        public LocalDateTime getOccurredAt() {
                return occurredAt;
        }

        public static class Builder {
                private Long knowledgeId;
                private Long factId;

                private boolean correct;
                private boolean helpUsed;

                private Long courseId;
                private Long courseQuestionAttemptId;
                private Integer questionRound;
                private LocalDateTime occurredAt;

                public Builder withKnowledgeId(Long knowledgeId) {
                        this.knowledgeId = knowledgeId;
                        return this;
                }

                public Builder withFactId(Long factId) {
                        this.factId = factId;
                        return this;
                }

                public Builder withCorrect(boolean correct) {
                        this.correct = correct;
                        return this;
                }

                public Builder withHelpUsed(boolean helpUsed) {
                        this.helpUsed = helpUsed;
                        return this;
                }

                public Builder withCourseId(Long courseId) {
                        this.courseId = courseId;
                        return this;
                }

                public Builder withCourseQuestionAttemptId(Long courseQuestionAttemptId) {
                        this.courseQuestionAttemptId = courseQuestionAttemptId;
                        return this;
                }

                public Builder withQuestionRound(Integer questionRound) {
                        this.questionRound = questionRound;
                        return this;
                }

                public Builder withOccurredAt(LocalDateTime occurredAt) {
                        this.occurredAt = occurredAt;
                        return this;
                }

                public KnowledgeResultEvent build() {
                        return new KnowledgeResultEvent(this);
                }
        }

        @Override
        public boolean equals(Object o) {
                if (this == o)
                        return true;
                if (!(o instanceof KnowledgeResultEvent))
                        return false;
                KnowledgeResultEvent that = (KnowledgeResultEvent) o;
                return correct == that.correct
                                && helpUsed == that.helpUsed
                                && Objects.equals(knowledgeId, that.knowledgeId)
                                && Objects.equals(factId, that.factId)
                                && Objects.equals(courseId, that.courseId)
                                && Objects.equals(courseQuestionAttemptId, that.courseQuestionAttemptId)
                                && Objects.equals(questionRound, that.questionRound)
                                && Objects.equals(occurredAt, that.occurredAt);
        }

        @Override
        public int hashCode() {
                return Objects.hash(knowledgeId, factId, correct, helpUsed, courseId, courseQuestionAttemptId,
                                questionRound, occurredAt);
        }

        @Override
        public String toString() {
                return "KnowledgeResultEvent{" +
                                "knowledgeId=" + knowledgeId +
                                ", factId=" + factId +
                                ", correct=" + correct +
                                ", helpUsed=" + helpUsed +
                                ", courseId=" + courseId +
                                ", courseQuestionAttemptId=" + courseQuestionAttemptId +
                                ", questionRound=" + questionRound +
                                ", occurredAt=" + occurredAt +
                                '}';
        }
}