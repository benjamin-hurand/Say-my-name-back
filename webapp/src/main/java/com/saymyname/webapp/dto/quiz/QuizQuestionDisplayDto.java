package com.saymyname.webapp.dto.quiz;

public record QuizQuestionDisplayDto(
        String prompt,
        String subtitle,
        String inputPlaceholder,
        Boolean timed,
        Integer timeLimitMs) {
}
