// src/main/java/com/saymyname/core/model/course/CourseStats.java
package com.saymyname.core.model.course;

import java.time.LocalDateTime;
import com.saymyname.core.model.enums.PopulationScope;

public class CourseStats {

    private Long courseId;
    private Long userId;
    private Long gameModeId;
    private PopulationScope populationScope;

    // Portée (candidats) et global
    private long totalCandidates; // ex. nb suivis si FOLLOWED, sinon nb total persons
    private long totalPersonsGlobal; // nb total de persons dans la base

    // Répartition des knowledges (créés)
    private int unknown;
    private int discovered;
    private int learned;
    private int mastered;
    private int createdTotal;

    // Ratios utiles à l'UI
    private double createdCoverageRatio; // createdTotal / totalCandidates
    private double masteredRatio; // mastered / createdTotal

    // Activité
    private int totalAnswers; // nb d’answers au total (historiques)
    private int answersToday; // nb d’answers aujourd’hui
    private LocalDateTime lastActivity;

    private int currentRound; // round courant (info du course)

    public static class Builder {
        private final CourseStats s = new CourseStats();

        public Builder withCourseId(Long v) {
            s.courseId = v;
            return this;
        }

        public Builder withUserId(Long v) {
            s.userId = v;
            return this;
        }

        public Builder withGameModeId(Long v) {
            s.gameModeId = v;
            return this;
        }

        public Builder withPopulationScope(PopulationScope v) {
            s.populationScope = v;
            return this;
        }

        public Builder withTotalCandidates(long v) {
            s.totalCandidates = v;
            return this;
        }

        public Builder withTotalPersonsGlobal(long v) {
            s.totalPersonsGlobal = v;
            return this;
        }

        public Builder withUnknown(int v) {
            s.unknown = v;
            return this;
        }

        public Builder withDiscovered(int v) {
            s.discovered = v;
            return this;
        }

        public Builder withLearned(int v) {
            s.learned = v;
            return this;
        }

        public Builder withMastered(int v) {
            s.mastered = v;
            return this;
        }

        public Builder withCreatedTotal(int v) {
            s.createdTotal = v;
            return this;
        }

        public Builder withCreatedCoverageRatio(double v) {
            s.createdCoverageRatio = v;
            return this;
        }

        public Builder withMasteredRatio(double v) {
            s.masteredRatio = v;
            return this;
        }

        public Builder withTotalAnswers(int v) {
            s.totalAnswers = v;
            return this;
        }

        public Builder withAnswersToday(int v) {
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

        public CourseStats build() {
            return s;
        }
    }

    // Getters simples (omits setters pour concision)
    public Long getCourseId() {
        return courseId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getGameModeId() {
        return gameModeId;
    }

    public PopulationScope getPopulationScope() {
        return populationScope;
    }

    public long getTotalCandidates() {
        return totalCandidates;
    }

    public long getTotalPersonsGlobal() {
        return totalPersonsGlobal;
    }

    public int getUnknown() {
        return unknown;
    }

    public int getDiscovered() {
        return discovered;
    }

    public int getLearned() {
        return learned;
    }

    public int getMastered() {
        return mastered;
    }

    public int getCreatedTotal() {
        return createdTotal;
    }

    public double getCreatedCoverageRatio() {
        return createdCoverageRatio;
    }

    public double getMasteredRatio() {
        return masteredRatio;
    }

    public int getTotalAnswers() {
        return totalAnswers;
    }

    public int getAnswersToday() {
        return answersToday;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public int getCurrentRound() {
        return currentRound;
    }
}
