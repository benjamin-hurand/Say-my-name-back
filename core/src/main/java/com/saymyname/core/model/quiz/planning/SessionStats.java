package com.saymyname.core.model.quiz.planning;

import com.saymyname.core.model.enums.quiz.QuizFormat;

/**
 * Session-level tracking data for question planning.
 * Captures metrics within a single play session (e.g., a course study session).
 */
public record SessionStats(
        int answered,              // Questions answered this session
        int correctStreak,         // Current consecutive correct answers
        int errorStreak,           // Current consecutive errors
        double accuracy,           // Correct / total this session (0.0-1.0)
        long durationMs,           // Time since session start
        int formatStreak,          // Consecutive same-format questions
        QuizFormat lastFormat      // Last format used (null if none)
) {

    /**
     * Empty session stats (start of session).
     */
    public static SessionStats empty() {
        return new SessionStats(0, 0, 0, 0.0, 0, 0, null);
    }

    /**
     * Builder for fluent construction.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int answered = 0;
        private int correctStreak = 0;
        private int errorStreak = 0;
        private double accuracy = 0.0;
        private long durationMs = 0;
        private int formatStreak = 0;
        private QuizFormat lastFormat = null;

        public Builder answered(int val) { this.answered = val; return this; }
        public Builder correctStreak(int val) { this.correctStreak = val; return this; }
        public Builder errorStreak(int val) { this.errorStreak = val; return this; }
        public Builder accuracy(double val) { this.accuracy = val; return this; }
        public Builder durationMs(long val) { this.durationMs = val; return this; }
        public Builder formatStreak(int val) { this.formatStreak = val; return this; }
        public Builder lastFormat(QuizFormat val) { this.lastFormat = val; return this; }

        public SessionStats build() {
            return new SessionStats(answered, correctStreak, errorStreak,
                    accuracy, durationMs, formatStreak, lastFormat);
        }
    }
}
