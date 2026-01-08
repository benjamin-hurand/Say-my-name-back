// src/main/java/com/saymyname/webapp/dto/quiz/QuizQuestionPayloadDto.java
package com.saymyname.webapp.dto.quiz;

import java.util.List;

import com.saymyname.core.model.enums.quiz.QuizOrderingRule;

public record QuizQuestionPayloadDto(

        // TEXT_INPUT
        TextInputPayload textInput,

        // CLOZE / HANGMAN
        ClozePayload cloze,
        HangmanPayload hangman,

        // MCQ / TAP_CHOICE
        ChoicePayload choices,

        // BINARY_SWIPE
        BinarySwipePayload binarySwipe,

        // ASSOCIATION
        AssociationPayload association,

        // ORDERING
        OrderingPayload ordering) {

    // -------- Sub payloads --------

    public record TextInputPayload(
            Long targetPersonId,
            String targetPhotoUrl) {
    }

    public record ClozePayload(
            Long targetPersonId,
            String targetPhotoUrl,
            String mask) {
    }

    public record HangmanPayload(
            Long targetPersonId,
            String targetPhotoUrl,
            String mask,
            Integer maxErrors) {
    }

    public record ChoicePayload(
            // pour MCQ / TAP_CHOICE
            List<ChoiceDto> choices,
            Boolean allowMultiple) {
    }

    public record BinarySwipePayload(
            ChoiceDto proposition) {
    }

    public record AssociationPayload(
            List<ItemDto> items) {
    }

    public record OrderingPayload(
            List<ItemDto> items,
            QuizOrderingRule orderBy) {
    }

    /**
     * Item multi-target (association/ordering).
     * labelId peut rester String si c’est un id de ressource; sinon passe-le en
     * Long.
     */
    public record ItemDto(
            Long personId,
            String photoUrl,
            String label) {
    }
}
