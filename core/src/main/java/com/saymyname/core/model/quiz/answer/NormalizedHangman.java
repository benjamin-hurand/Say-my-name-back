package com.saymyname.core.model.quiz.answer;

import com.saymyname.core.model.enums.quiz.HangmanAction;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class NormalizedHangman implements NormalizedAudit {
    HangmanAction action;
    String letter;
    String value;

    @Override
    public String auditString() {
        if (action == HangmanAction.GUESS) {
            return "GUESS:" + (letter != null ? letter : "null");
        }
        if (action == HangmanAction.SOLVE) {
            return "SOLVE:" + (value != null ? value : "null");
        }
        return "INVALID";
    }
}
