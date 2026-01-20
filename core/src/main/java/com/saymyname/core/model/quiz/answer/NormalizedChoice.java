package com.saymyname.core.model.quiz.answer;

public record NormalizedChoice(
        Long selectedChoiceId,
        String selectedValue) implements NormalizedAudit {

    @Override
    public String auditString() {
        if (selectedChoiceId == null)
            return null;
        return selectedChoiceId + ":" + (selectedValue == null ? "" : selectedValue);
    }
}
