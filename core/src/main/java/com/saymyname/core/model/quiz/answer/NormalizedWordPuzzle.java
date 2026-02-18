package com.saymyname.core.model.quiz.answer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class NormalizedWordPuzzle implements NormalizedAudit {
    String word;

    @Override
    public String auditString() {
        return "WORD:" + (word != null ? word : "null");
    }
}
