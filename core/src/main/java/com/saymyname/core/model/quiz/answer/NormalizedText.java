// src/main/java/com/saymyname/core/model/quiz/answer/NormalizedText.java
package com.saymyname.core.model.quiz.answer;

public record NormalizedText(String raw, String canonical) implements NormalizedAudit {

    @Override
    public String auditString() {
        // on stocke le canonical (stable), et raw est pour debug local si besoin
        return canonical;
    }
}
