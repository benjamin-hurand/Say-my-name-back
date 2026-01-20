// src/main/java/com/saymyname/core/model/course/KnowledgeResultEvent.java
package com.saymyname.core.model.course;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Event "SRS / Knowledge update" produced by a question result.
 * Immutable-ish (no setters) and built via Builder.
 *
 * Recommended:
 * - prefer knowledgeId when available (fast path).
 * - fallback to (gameModeId, personId) when knowledgeId is null.
 */
public class KnowledgeResultEvent {

        private final Long knowledgeId; // optional but preferred
        private final Long gameModeId; // required if knowledgeId is null
        private final Long personId; // required if knowledgeId is null
        private final boolean correct;
        private final boolean helpUsed;

        // Optional context (useful for analytics/debug)
        private final Long courseId;
        private final Long CourseQuestionAttemptId;
        private final Integer questionRound;
        private final LocalDateTime occurredAt;

        private KnowledgeResultEvent(Builder b) {
                this.knowledgeId = b.knowledgeId;
                this.gameModeId = b.gameModeId;
                this.personId = b.personId;
                this.correct = b.correct;
                this.helpUsed = b.helpUsed;
                this.courseId = b.courseId;
                this.CourseQuestionAttemptId = b.CourseQuestionAttemptId;
                this.questionRound = b.questionRound;
                this.occurredAt = b.occurredAt != null ? b.occurredAt : LocalDateTime.now();

                // Validation minimale:
                if (this.knowledgeId == null) {
                        if (this.gameModeId == null || this.personId == null) {
                                throw new IllegalArgumentException(
                                                "knowledgeId is null => gameModeId and personId are required");
                        }
                }
        }

        public Long getKnowledgeId() {
                return knowledgeId;
        }

        public Long getGameModeId() {
                return gameModeId;
        }

        public Long getPersonId() {
                return personId;
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
                return CourseQuestionAttemptId;
        }

        public Integer getQuestionRound() {
                return questionRound;
        }

        public LocalDateTime getOccurredAt() {
                return occurredAt;
        }

        public static class Builder {
                private Long knowledgeId;
                private Long gameModeId;
                private Long personId;
                private boolean correct;
                private boolean helpUsed;

                private Long courseId;
                private Long CourseQuestionAttemptId;
                private Integer questionRound;
                private LocalDateTime occurredAt;

                public Builder withKnowledgeId(Long knowledgeId) {
                        this.knowledgeId = knowledgeId;
                        return this;
                }

                public Builder withGameModeId(Long gameModeId) {
                        this.gameModeId = gameModeId;
                        return this;
                }

                public Builder withPersonId(Long personId) {
                        this.personId = personId;
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

                public Builder withCourseQuestionAttemptId(Long CourseQuestionAttemptId) {
                        this.CourseQuestionAttemptId = CourseQuestionAttemptId;
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
                                && Objects.equals(gameModeId, that.gameModeId)
                                && Objects.equals(personId, that.personId)
                                && Objects.equals(courseId, that.courseId)
                                && Objects.equals(CourseQuestionAttemptId, that.CourseQuestionAttemptId)
                                && Objects.equals(questionRound, that.questionRound)
                                && Objects.equals(occurredAt, that.occurredAt);
        }

        @Override
        public int hashCode() {
                return Objects.hash(knowledgeId, gameModeId, personId, correct, helpUsed,
                                courseId, CourseQuestionAttemptId, questionRound, occurredAt);
        }

        @Override
        public String toString() {
                return "KnowledgeResultEvent{" +
                                "knowledgeId=" + knowledgeId +
                                ", gameModeId=" + gameModeId +
                                ", personId=" + personId +
                                ", correct=" + correct +
                                ", helpUsed=" + helpUsed +
                                ", courseId=" + courseId +
                                ", CourseQuestionAttemptId=" + CourseQuestionAttemptId +
                                ", questionRound=" + questionRound +
                                ", occurredAt=" + occurredAt +
                                '}';
        }
}
