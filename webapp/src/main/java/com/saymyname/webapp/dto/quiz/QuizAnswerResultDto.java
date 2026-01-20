// src/main/java/com/saymyname/webapp/dto/quiz/QuizAnswerResultDto.java
package com.saymyname.webapp.dto.quiz;

import java.util.List;

import com.saymyname.webapp.dto.leaderboard.XpAwardDto;

public record QuizAnswerResultDto(
                boolean correct,
                String feedbackMessage,
                QuizQuestionDto nextQuestion,
                List<QuizAnswerItemResultDto> itemResults,
                Boolean isComplete, // null=single-shot, true/false=multi-step
                MultiStepStateDto currentState,
                XpAwardDto xpAward) {
}
