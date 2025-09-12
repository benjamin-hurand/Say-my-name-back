package com.saymyname.webapp.mapper.challenge;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.challenge.ChallengeHistoryEntry;
import com.saymyname.core.model.people.Person;
import com.saymyname.webapp.dto.challenge.ChallengeHistoryEntryDto;

@Component
public class ChallengeHistoryEntryDtoMapper {

    public ChallengeHistoryEntryDto toDto(ChallengeHistoryEntry model) {
        return new ChallengeHistoryEntryDto(
                model.getQuestionNumber(),
                model.getPerson().getId(),
                model.getAnswer());
    }

    public ChallengeHistoryEntry toModel(ChallengeHistoryEntryDto dto) {
        return new ChallengeHistoryEntry.Builder()
                .withQuestionNumber(dto.questionNumber())
                .withPerson(new Person.Builder().withId(dto.personId()).build())
                .withAnswer(dto.answer())
                .build();
    }
}
