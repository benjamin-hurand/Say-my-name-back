package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

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
    
}
