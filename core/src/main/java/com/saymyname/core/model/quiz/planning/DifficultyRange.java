package com.saymyname.core.model.quiz.planning;

/**
 * Represents a difficulty range constraint for question planning.
 * Used to clamp or validate target difficulty values.
 */
public record DifficultyRange(double min, double max) {

    public DifficultyRange {
        if (min < 0.0 || min > 1.0) {
            throw new IllegalArgumentException("min must be between 0.0 and 1.0");
        }
        if (max < 0.0 || max > 1.0) {
            throw new IllegalArgumentException("max must be between 0.0 and 1.0");
        }
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }
    }

    /**
     * Clamp a difficulty value to this range.
     */
    public double clamp(double d) {
        return Math.max(min, Math.min(max, d));
    }

    /**
     * Check if a difficulty value is within this range.
     */
    public boolean contains(double d) {
        return d >= min && d <= max;
    }

    /**
     * Full range from 0.0 to 1.0 (no constraint).
     */
    public static DifficultyRange full() {
        return new DifficultyRange(0.0, 1.0);
    }

    /**
     * Easy range (low difficulty).
     */
    public static DifficultyRange easy() {
        return new DifficultyRange(0.1, 0.35);
    }

    /**
     * Medium range (moderate difficulty).
     */
    public static DifficultyRange medium() {
        return new DifficultyRange(0.35, 0.65);
    }

    /**
     * Hard range (high difficulty).
     */
    public static DifficultyRange hard() {
        return new DifficultyRange(0.65, 0.9);
    }
}
