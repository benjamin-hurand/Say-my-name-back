// src/main/java/com/saymyname/service/quiz/plugins/TapChoicePlugin.java
package com.saymyname.service.quiz.plugins;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.service.quiz.AnswerKeyService;

public class TapChoicePlugin extends McqPlugin {
    public TapChoicePlugin(AnswerKeyService answerKeyService) {
        super(answerKeyService);
    }

    @Override
    public QuizFormat supports() {
        return QuizFormat.TAP_CHOICE;
    }
}
