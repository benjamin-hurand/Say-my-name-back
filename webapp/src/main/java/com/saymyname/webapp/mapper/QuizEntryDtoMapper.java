package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.challenge.ChallengeQuestion;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.game.QuizEntry;
import com.saymyname.webapp.dto.QuizEntryDto;

@Component
public class QuizEntryDtoMapper {

    public QuizEntry toModel(QuizEntryDto quizEntryDto) {
        return new QuizEntry.Builder()
                .withPersonId(quizEntryDto.personId())
                .withPhotoUrl(quizEntryDto.photoUrl())
                .withInitials(quizEntryDto.initials())
                .build();
    }

    public QuizEntryDto toDto(QuizEntry quizEntry) {
        return new QuizEntryDto(quizEntry.getPersonId(), quizEntry.getPhotoUrl(), quizEntry.getInitials());
    }

    public QuizEntryDto toDto(ChallengeQuestion challengeQuestion, String initials) {
        return new QuizEntryDto(challengeQuestion.getPerson().getId(),
                challengeQuestion.getPerson().getPhoto().getUrl(), initials);
    }

    public QuizEntryDto toDto(Knowledge knowledge, String initials) {
        return new QuizEntryDto(knowledge.getPerson().getId(), knowledge.getPerson().getPhoto().getUrl(), initials);
    }

}
