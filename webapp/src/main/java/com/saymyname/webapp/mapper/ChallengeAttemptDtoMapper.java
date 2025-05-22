package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.challenge.ChallengeAttempt;
import com.saymyname.core.model.challenge.ChallengeVersion;
import com.saymyname.core.model.common.User;
import com.saymyname.core.model.enums.AttemptStatus;
import com.saymyname.core.util.InitialCrafter;
import com.saymyname.webapp.dto.AddChallengeAttemptDto;
import com.saymyname.webapp.dto.CreatedChallengeAttemptDto;
import com.saymyname.webapp.dto.QuizEntryDto;

@Component
public class ChallengeAttemptDtoMapper {

    private final QuizEntryDtoMapper quizEntryDtoMapper;
    private final InitialCrafter initialCrafter;

    public ChallengeAttemptDtoMapper(QuizEntryDtoMapper quizEntryDtoMapper, InitialCrafter initialCrafter) {
        this.quizEntryDtoMapper = quizEntryDtoMapper;
        this.initialCrafter = initialCrafter;
    }

    public ChallengeAttempt toModel(AddChallengeAttemptDto dto) {
        return new ChallengeAttempt.Builder()
                .withUser(new User.Builder().withId(dto.userId()).build())
                .withChallengeVersion(new ChallengeVersion.Builder().withId(dto.challengeVersionId()).build())
                .withStatus(AttemptStatus.IN_PROGRESS)
                .build();
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
                attempt.getUser().getId(),
                attempt.getChallengeVersion().getId(),
                questionDtos);
    }
}
