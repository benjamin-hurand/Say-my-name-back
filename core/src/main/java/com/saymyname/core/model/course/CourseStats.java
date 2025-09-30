// src/main/java/com/saymyname/core/model/course/CourseStats.java
package com.saymyname.core.model.course;

import java.time.LocalDateTime;

public class CourseStats {

    private Long courseId;
    private Long gameModeId;

    // Éligibles pour CE course (dépend du game mode + scope suivi)
    private long totalCandidates; // ex. # suivis éligibles pour ce mode
    private long universeEligible; // ex. # personnes éligibles globalement pour ce mode

    // Répartition des knowledges
    private long unknown;
    private long discovered;
    private long learned;
    private long mastered;

    // Activité
    private long totalAnswers;
    private long answersToday;
    private LocalDateTime lastActivity;

    // État du course
    private int currentRound;

    // Indicateur utile (non dérivable simplement)
    private long dueNow; // ex. # items SRS “dus” maintenant pour ce course

    public static class Builder {
        private final CourseStats s = new CourseStats();

        public Builder withCourseId(Long v) {
            s.courseId = v;
            return this;
        }

        public Builder withGameModeId(Long v) {
            s.gameModeId = v;
            return this;
        }

        public Builder withTotalCandidates(long v) {
            s.totalCandidates = v;
            return this;
        }

        public Builder withUniverseEligible(long v) {
            s.universeEligible = v;
            return this;
        }

        public Builder withUnknown(long v) {
            s.unknown = v;
            return this;
        }

        public Builder withDiscovered(long v) {
            s.discovered = v;
            return this;
        }

        public Builder withLearned(long v) {
            s.learned = v;
            return this;
        }

        public Builder withMastered(long v) {
            s.mastered = v;
            return this;
        }

        public Builder withTotalAnswers(long v) {
            s.totalAnswers = v;
            return this;
        }

        public Builder withAnswersToday(long v) {
            s.answersToday = v;
            return this;
        }

        public Builder withLastActivity(LocalDateTime v) {
            s.lastActivity = v;
            return this;
        }

        public Builder withCurrentRound(int v) {
            s.currentRound = v;
            return this;
        }

        public Builder withDueNow(long v) {
            s.dueNow = v;
            return this;
        }

        public CourseStats build() {
            return s;
        }
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getGameModeId() {
        return gameModeId;
    }

    public long getTotalCandidates() {
        return totalCandidates;
    }

    public long getUniverseEligible() {
        return universeEligible;
    }

    public long getUnknown() {
        return unknown;
    }

    public long getDiscovered() {
        return discovered;
    }

    public long getLearned() {
        return learned;
    }

    public long getMastered() {
        return mastered;
    }

    public long getTotalAnswers() {
        return totalAnswers;
    }

    public long getAnswersToday() {
        return answersToday;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public long getDueNow() {
        return dueNow;
    }
}
