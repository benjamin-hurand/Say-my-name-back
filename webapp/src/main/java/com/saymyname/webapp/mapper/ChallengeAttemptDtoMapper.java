package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;
import com.saymyname.core.model.challenge.ChallengeAttempt;
import com.saymyname.core.model.challenge.ChallengeQuestion;
import com.saymyname.core.model.challenge.ChallengeVersion;
import com.saymyname.core.model.common.User;
import com.saymyname.webapp.dto.AddChallengeAttemptDto;
import com.saymyname.webapp.dto.ChallengeQuestionDto;
import com.saymyname.webapp.dto.CreatedChallengeAttemptDto;

@Component
public class ChallengeAttemptDtoMapper {

    public ChallengeAttempt toModel(AddChallengeAttemptDto dto) {
        return new ChallengeAttempt.Builder()
                .withUser(new User.Builder().withId(dto.userId()).build())
                .withChallengeVersion(new ChallengeVersion.Builder().withId(dto.challengeVersionId()).build())
                .build();
    }

    public CreatedChallengeAttemptDto toDto(ChallengeAttempt attempt) {
        ChallengeQuestionDto[] questionDtos;
        if (attempt.getChallengeVersion().getQuestions() != null) {
            questionDtos = attempt.getChallengeVersion().getQuestions().stream()
                    .map(q -> new ChallengeQuestionDto(q.getPerson().getId(), q.getPerson().getPhoto().getUrl()))
                    .toArray(ChallengeQuestionDto[]::new);
        } else {
            questionDtos = new ChallengeQuestionDto[0];
        }

        return new CreatedChallengeAttemptDto(
                attempt.getId(),
                attempt.getUser().getId(),
                attempt.getChallengeVersion().getId(),
                questionDtos);
    }
}
