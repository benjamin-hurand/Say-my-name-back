package com.saymyname.webapp.dto.quiz;

import com.saymyname.core.model.enums.quiz.QuizFollowUpReason;
import com.saymyname.core.model.enums.quiz.QuizFollowUpStrategy;

public record QuizFollowUpDto(
                QuizFollowUpStrategy strategy,
                QuizFollowUpReason reason) {
}
