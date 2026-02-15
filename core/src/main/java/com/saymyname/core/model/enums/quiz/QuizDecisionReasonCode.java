package com.saymyname.core.model.enums.quiz;

public enum QuizDecisionReasonCode {
    EXPLICIT_USER,

    AUTO_UNKNOWN_BOOTSTRAP,
    AUTO_DISCOVERED_BOOTSTRAP,
    AUTO_LEARNED_FLUENCY_TIMED,
    AUTO_LEARNED_EASY,
    AUTO_LEARNED,
    AUTO_MASTERED_SINGLE,
    AUTO_MASTERED_MULTI_TARGET,

    FALLBACK_MULTI_TARGET_GATE,
    FALLBACK_MULTI_TARGET_SHORTFALL,
    ANTI_MONOTONY_FORMAT_VARIATION,

    // D1: Ladder format codes (recognition -> recall -> transfer)
    LADDER_RECOGNITION_MCQ,
    LADDER_RECOGNITION_SWIPE,
    LADDER_RECALL_TEXT,
    LADDER_RECALL_CLOZE,
    LADDER_TRANSFER_ASSOCIATION,
    LADDER_TRANSFER_ORDERING,

    // D3: Timed gating
    TIMED_BLOCKED_HIGH_STRESS,

    // D4: Anti-monotony format switch
    ANTI_MONOTONY_FORMAT_SWITCH,

    // D6: Guardrail - fragile user protection
    GUARDRAIL_FRAGILE_NO_ESCALATE,

    TRAINING_AUTO_DEFAULT,
    TRAINING_EXPLICIT_USER;

    public static QuizDecisionReasonCode fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        try {
            return QuizDecisionReasonCode.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return switch (normalized) {
                case "EXPLICIT", "EXPLICIT_MULTI_TARGET_UNAVAILABLE", "OVERRIDE_TIMED" -> EXPLICIT_USER;
                case "AUTO_LEARNED_FLUENCY" -> AUTO_LEARNED_FLUENCY_TIMED;
                case "AUTO_MASTERED" -> AUTO_MASTERED_SINGLE;
                case "TRAINING_EXPLICIT" -> TRAINING_EXPLICIT_USER;
                case "TRAINING_EASY", "TRAINING_EASY_STRESS",
                        "TRAINING_LEARNED", "TRAINING_LEARNED_FLUENT",
                        "TRAINING_MASTERED", "TRAINING_MASTERED_FLUENT" -> TRAINING_AUTO_DEFAULT;
                default -> throw new IllegalArgumentException("Unknown QuizDecisionReasonCode: " + normalized);
            };
        }
    }
}
