package com.saymyname.webapp.mapper.challenge;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.challenge.ChallengeAttempt;
import com.saymyname.core.util.InitialCrafter;
import com.saymyname.webapp.dto.QuizEntryDto;
import com.saymyname.webapp.dto.challenge.CreatedChallengeAttemptDto;
import com.saymyname.webapp.mapper.QuizEntryDtoMapper;

@Component
public class ChallengeAttemptDtoMapper {

    private final QuizEntryDtoMapper quizEntryDtoMapper;
    private final InitialCrafter initialCrafter;

    public ChallengeAttemptDtoMapper(QuizEntryDtoMapper quizEntryDtoMapper, InitialCrafter initialCrafter) {
        this.quizEntryDtoMapper = quizEntryDtoMapper;
        this.initialCrafter = initialCrafter;
    }

    public CreatedChallengeAttemptDto toDto(ChallengeAttempt attempt) {
        QuizEntryDto[] questionDtos;
        if (attempt.getChallengeVersion().getQuestions() != null) {
            questionDtos = attempt.getChallengeVersion().getQuestions().stream()
                    .map(question -> {
                        // Suppose initialCrafter is injected as a dependency
                        String initials = initialCrafter.computeInitials(question.getPerson(),
                                attempt.getChallengeVersion().getChallenge().getGameMode());
                        return quizEntryDtoMapper.toDto(question, initials);
                    })
                    .toArray(QuizEntryDto[]::new);
        } else {
            questionDtos = new QuizEntryDto[0];
        }

        return new CreatedChallengeAttemptDto(
                attempt.getId(),
                attempt.getChallengeVersion().getId(),
                questionDtos);
    }
}
