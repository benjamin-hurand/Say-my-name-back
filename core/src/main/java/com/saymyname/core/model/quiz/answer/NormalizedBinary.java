package com.saymyname.core.model.quiz.answer;

public record NormalizedBinary(Boolean swipeRight) implements NormalizedAudit {

    @Override
    public String auditString() {
        if (swipeRight == null)
            return null;
        return swipeRight ? "true" : "false";
    }
}
