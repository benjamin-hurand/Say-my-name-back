package com.saymyname.core.model.quiz.answer;

public record NormalizedBinary(Boolean swipeRight) implements NormalizedSubmission {

    @Override
    public String auditString() {
        if (swipeRight == null)
            return null;
        return swipeRight ? "true" : "false";
    }
}
