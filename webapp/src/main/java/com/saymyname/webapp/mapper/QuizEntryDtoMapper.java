package com.saymyname.webapp.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.game.QuizEntry;
import com.saymyname.core.model.people.Person;
import com.saymyname.webapp.dto.QuizEntryDto;

@Component
public class QuizEntryDtoMapper {

    @Value("${photos.storage.public-base-url}")
    private String photosBaseUrl;

    public QuizEntryDto toDto(QuizEntry quizEntry) {
        String fullUrl = toPublicUrl(quizEntry.getStorageKey());
        return new QuizEntryDto(quizEntry.getPersonId(), fullUrl, quizEntry.getInitials());
    }

    public QuizEntryDto toDto(Knowledge knowledge, String initials) {
        Person person = knowledge.getPerson();
        String fullUrl = person.getApprovedPhoto()
                .map(photo -> toPublicUrl(photo.getStorageKey()))
                .orElse(null);

        return new QuizEntryDto(person.getId(), fullUrl, initials);
    }

    /**
     * Construit l’URL publique à partir d’une storageKey. Retourne null si key
     * vide.
     */
    private String toPublicUrl(String storageKey) {
        if (storageKey == null || storageKey.isBlank())
            return null;
        if (photosBaseUrl == null || photosBaseUrl.isBlank())
            return storageKey; // fallback minimal
        return photosBaseUrl.endsWith("/") ? photosBaseUrl + storageKey : photosBaseUrl + "/" + storageKey;
    }
}
