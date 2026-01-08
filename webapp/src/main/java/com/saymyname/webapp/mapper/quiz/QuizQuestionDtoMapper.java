// src/main/java/com/saymyname/webapp/mapper/quiz/QuizQuestionDtoMapper.java
package com.saymyname.webapp.mapper.quiz;

import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.*;
import com.saymyname.service.photo.PhotoUrlResolver;
import com.saymyname.webapp.dto.quiz.*;

@Component
public class QuizQuestionDtoMapper {

    private final PhotoUrlResolver photoUrlResolver;

    public QuizQuestionDtoMapper(PhotoUrlResolver photoUrlResolver) {
        this.photoUrlResolver = photoUrlResolver;
    }

    public QuizQuestionDto toDto(QuizQuestion q) {
        if (q == null)
            return null;

        QuizQuestionContextDto ctxDto = toContextDto(q.getContext());
        QuizQuestionPayloadDto payloadDto = toPayloadDto(q);
        QuizQuestionHintsDto hintsDto = toHintsDto(q.getHints());
        QuizQuestionDisplayDto displayDto = toDisplayDto(q.getDisplay());
        QuizFollowUpDto followUpDto = toFollowUpDto(q.getFollowUp());
        String reasonCode = q.getReasonCode() != null ? q.getReasonCode().name() : null;

        return new QuizQuestionDto(
                q.getQuestionToken(),
                q.getGameModeId(),
                ctxDto,
                q.getFormat(),
                payloadDto,
                hintsDto,
                displayDto,
                followUpDto,
                reasonCode,
                q.getReasonDetailsJson());
    }

    public List<QuizQuestionDto> toDtoList(List<QuizQuestion> list) {
        if (list == null)
            return List.of();
        return list.stream().map(this::toDto).toList();
    }

    private QuizQuestionContextDto toContextDto(QuizQuestionContext ctx) {
        if (ctx == null)
            return null;
        return new QuizQuestionContextDto(
                ctx.getSource(),
                ctx.getCourseId(),
                ctx.getCourseQuestionId(),
                ctx.getQuestionRound(),
                ctx.getPoolType(),
                ctx.getDifficultyLevel(),
                ctx.getReducedOptionsId());
    }

    /**
     * DTO lean: payload = union de sous-payloads.
     * - Suppression payload.type (redondant avec format).
     * - Les infos single-target (personId/photo) sont portées par les
     * sous-payloads.
     */
    private QuizQuestionPayloadDto toPayloadDto(QuizQuestion q) {
        QuizQuestionPayload p = q.getPayload();
        if (p == null)
            return null;

        QuizFormat format = q.getFormat();

        // Single-target info (core)
        Long targetPersonId = q.getPersonId();
        String targetPhotoUrl = q.getStorageKey() != null ? photoUrlResolver.smallUrl(q.getStorageKey()) : null;

        // Choices
        List<ChoiceDto> choices = null;
        if (p.getChoices() != null) {
            choices = p.getChoices().stream()
                    .map(c -> new ChoiceDto(c.getId(), c.getLabel(), c.getValue(), c.getPersonId()))
                    .toList();
        }

        // Proposition (swipe)
        ChoiceDto proposition = null;
        if (p.getProposition() != null) {
            var c = p.getProposition();
            proposition = new ChoiceDto(c.getId(), c.getLabel(), c.getValue(), c.getPersonId());
        }

        // Items (association/ordering)
        List<QuizQuestionPayloadDto.ItemDto> items = null;
        if (p.getItems() != null) {
            items = p.getItems().stream()
                    .map(it -> new QuizQuestionPayloadDto.ItemDto(
                            it.getPersonId(),
                            it.getStorageKey() != null ? photoUrlResolver.smallUrl(it.getStorageKey()) : null,
                            it.getLabelId() // ✅ ton modèle a labelId, pas label
                    ))
                    .toList();
        }

        // Subpayloads
        QuizQuestionPayloadDto.TextInputPayload textInput = null;
        QuizQuestionPayloadDto.ClozePayload cloze = null;
        QuizQuestionPayloadDto.HangmanPayload hangman = null;
        QuizQuestionPayloadDto.ChoicePayload choicePayload = null;
        QuizQuestionPayloadDto.BinarySwipePayload binarySwipe = null;
        QuizQuestionPayloadDto.AssociationPayload association = null;
        QuizQuestionPayloadDto.OrderingPayload ordering = null;

        if (format != null) {
            switch (format) {
                case TEXT_INPUT ->
                    textInput = new QuizQuestionPayloadDto.TextInputPayload(targetPersonId, targetPhotoUrl);

                case CLOZE ->
                    cloze = new QuizQuestionPayloadDto.ClozePayload(targetPersonId, targetPhotoUrl, p.getMask());

                case HANGMAN -> hangman = new QuizQuestionPayloadDto.HangmanPayload(targetPersonId, targetPhotoUrl,
                        p.getMask(), p.getMaxErrors());

                case MCQ, TAP_CHOICE ->
                    choicePayload = new QuizQuestionPayloadDto.ChoicePayload(choices, p.getAllowMultiple());

                case BINARY_SWIPE -> binarySwipe = new QuizQuestionPayloadDto.BinarySwipePayload(proposition);

                case ASSOCIATION -> association = new QuizQuestionPayloadDto.AssociationPayload(items);

                case ORDERING -> ordering = new QuizQuestionPayloadDto.OrderingPayload(items, p.getOrderBy());
            }
        } else {
            // fallback: on ne plante pas si format null
            choicePayload = (choices != null || p.getAllowMultiple() != null)
                    ? new QuizQuestionPayloadDto.ChoicePayload(choices, p.getAllowMultiple())
                    : null;
        }

        return new QuizQuestionPayloadDto(
                textInput,
                cloze,
                hangman,
                choicePayload,
                binarySwipe,
                association,
                ordering);
    }

    private QuizQuestionHintsDto toHintsDto(QuizQuestionHints h) {
        if (h == null)
            return null;
        return new QuizQuestionHintsDto(h.getInitials());
    }

    private QuizQuestionDisplayDto toDisplayDto(QuizQuestionDisplay d) {
        if (d == null)
            return null;
        return new QuizQuestionDisplayDto(
                d.getPrompt(),
                d.getSubtitle(),
                d.getInputPlaceholder(),
                d.getTimed(),
                d.getTimeLimitMs());
    }

    private QuizFollowUpDto toFollowUpDto(QuizFollowUp f) {
        if (f == null)
            return null;
        return new QuizFollowUpDto(f.getStrategy(), f.getReason());
    }
}
