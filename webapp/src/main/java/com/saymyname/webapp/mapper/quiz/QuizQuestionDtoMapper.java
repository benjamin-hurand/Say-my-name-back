// src/main/java/com/saymyname/webapp/mapper/quiz/QuizQuestionDtoMapper.java
package com.saymyname.webapp.mapper.quiz;

import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.QuizFollowUp;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionContext;
import com.saymyname.core.model.quiz.QuizQuestionDisplay;
import com.saymyname.core.model.quiz.QuizQuestionHints;
import com.saymyname.core.model.quiz.QuizQuestionPayload;
import com.saymyname.service.photo.PhotoUrlResolver;
import com.saymyname.webapp.dto.quiz.ChoiceDto;
import com.saymyname.webapp.dto.quiz.QuizFollowUpDto;
import com.saymyname.webapp.dto.quiz.QuizQuestionContextDto;
import com.saymyname.webapp.dto.quiz.QuizQuestionDisplayDto;
import com.saymyname.webapp.dto.quiz.QuizQuestionDto;
import com.saymyname.webapp.dto.quiz.QuizQuestionHintsDto;
import com.saymyname.webapp.dto.quiz.QuizQuestionPayloadDto;

@Component
public class QuizQuestionDtoMapper {

    private final PhotoUrlResolver photoUrlResolver;

    public QuizQuestionDtoMapper(PhotoUrlResolver photoUrlResolver) {
        this.photoUrlResolver = photoUrlResolver;
    }

    public QuizQuestionDto toDto(QuizQuestion q) {
        if (q == null) {
            return null;
        }

        QuizQuestionContextDto ctxDto = toContextDto(q.getContext());
        QuizQuestionPayloadDto payloadDto = toPayloadDto(q);
        QuizQuestionHintsDto hintsDto = toHintsDto(q.getHints());
        QuizQuestionDisplayDto displayDto = toDisplayDto(q.getDisplay());
        QuizFollowUpDto followUpDto = toFollowUpDto(q.getFollowUp());
        String reasonCode = q.getReasonCode() != null ? q.getReasonCode().name() : null;

        return new QuizQuestionDto(
                q.getQuestionHandle(),
                q.getTargetAttributeId(),
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
        if (list == null) {
            return List.of();
        }
        return list.stream().map(this::toDto).toList();
    }

    private QuizQuestionContextDto toContextDto(QuizQuestionContext ctx) {
        if (ctx == null) {
            return null;
        }
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
     * - CRITICAL: aucune truth ne doit fuiter (correct flags etc.)
     *
     * NOTE IMPORTANT:
     * - QuizQuestion (core) ne porte PAS l'état runtime multi-step
     * (hangmanState/wordPuzzleState).
     * - Donc: currentState (WORD_PUZZLE) doit rester null ici.
     * - Le mapping du state se fait depuis QuizQuestionSnapshot, dans un mapper
     * dédié.
     */
    private QuizQuestionPayloadDto toPayloadDto(QuizQuestion q) {
        QuizQuestionPayload p = q.getPayload();
        if (p == null) {
            return null;
        }

        QuizFormat format = q.getFormat();

        // Single-target info (core)
        Long targetPersonId = q.getPersonId();
        String targetPhotoUrl = q.getStorageKey() != null ? photoUrlResolver.largeUrl(q.getStorageKey()) : null;

        // Choices (CRITICAL: Filter out any truth data, map photos)
        List<ChoiceDto> choices = null;
        if (p.getChoices() != null) {
            choices = p.getChoices().stream()
                    .map(c -> new ChoiceDto(
                            c.getId(),
                            c.getLabel(),
                            c.getValue(),
                            c.getStorageKey() != null ? photoUrlResolver.largeUrl(c.getStorageKey()) : null,
                            c.getPersonId()
                    // CRITICAL: c.getCorrect() deliberately omitted - never expose truth to client
                    ))
                    .toList();
        }

        // Proposition (swipe) (CRITICAL: Filter out any truth data, map photos)
        ChoiceDto proposition = null;
        if (p.getProposition() != null) {
            var c = p.getProposition();
            proposition = new ChoiceDto(
                    c.getId(),
                    c.getLabel(),
                    c.getValue(),
                    c.getStorageKey() != null ? photoUrlResolver.largeUrl(c.getStorageKey()) : null,
                    c.getPersonId()
            // CRITICAL: c.getCorrect() deliberately omitted - never expose truth to client
            );
        }

        // Items (association/ordering)
        List<QuizQuestionPayloadDto.ItemDto> items = null;
        if (p.getItems() != null) {
            items = p.getItems().stream()
                    .map(it -> new QuizQuestionPayloadDto.ItemDto(
                            it.getPersonId(),
                            it.getStorageKey() != null ? photoUrlResolver.largeUrl(it.getStorageKey()) : null,
                            it.getLabelId() // ✅ ton modèle core a labelId (String)
                    ))
                    .toList();
        }

        // Subpayloads
        QuizQuestionPayloadDto.TextInputPayload textInput = null;
        QuizQuestionPayloadDto.ClozePayload cloze = null;
        QuizQuestionPayloadDto.HangmanPayload hangman = null;
        QuizQuestionPayloadDto.WordPuzzlePayload wordPuzzle = null;

        QuizQuestionPayloadDto.ChoicePayload choicePayload = null;
        QuizQuestionPayloadDto.BinarySwipePayload binarySwipe = null;
        QuizQuestionPayloadDto.AssociationPayload association = null;
        QuizQuestionPayloadDto.OrderingPayload ordering = null;

        if (format != null) {
            switch (format) {

                case TEXT_INPUT -> {
                    textInput = new QuizQuestionPayloadDto.TextInputPayload(targetPersonId, targetPhotoUrl);
                }

                case CLOZE -> {
                    cloze = new QuizQuestionPayloadDto.ClozePayload(targetPersonId, targetPhotoUrl, p.getMask());
                }

                case HANGMAN -> {
                    hangman = new QuizQuestionPayloadDto.HangmanPayload(
                            targetPersonId,
                            targetPhotoUrl,
                            p.getMask(),
                            p.getMaxErrors());
                }

                case WORD_PUZZLE -> {
                    // QuizQuestion ne contient pas le state runtime : currentState = null ici.
                    wordPuzzle = new QuizQuestionPayloadDto.WordPuzzlePayload(
                            targetPersonId,
                            targetPhotoUrl,
                            p.getWordLength(),
                            p.getMaxAttempts(),
                            null);
                }

                case MCQ -> {
                    choicePayload = new QuizQuestionPayloadDto.ChoicePayload(
                            choices,
                            p.getAllowMultiple(),
                            targetPersonId,
                            targetPhotoUrl);
                }

                case BINARY_SWIPE -> {
                    binarySwipe = new QuizQuestionPayloadDto.BinarySwipePayload(
                            proposition,
                            targetPersonId,
                            targetPhotoUrl);
                }

                case ASSOCIATION -> {
                    association = new QuizQuestionPayloadDto.AssociationPayload(items);
                }

                case ORDERING -> {
                    ordering = new QuizQuestionPayloadDto.OrderingPayload(items, p.getOrderBy());
                }
            }
        } else {
            // fallback: on ne plante pas si format null
            choicePayload = (choices != null || p.getAllowMultiple() != null)
                    ? new QuizQuestionPayloadDto.ChoicePayload(
                            choices,
                            p.getAllowMultiple(),
                            targetPersonId,
                            targetPhotoUrl)
                    : null;
        }

        return new QuizQuestionPayloadDto(
                textInput,
                cloze,
                hangman,
                wordPuzzle,
                choicePayload,
                binarySwipe,
                association,
                ordering);
    }

    private QuizQuestionHintsDto toHintsDto(QuizQuestionHints h) {
        if (h == null) {
            return null;
        }
        return new QuizQuestionHintsDto(h.getInitials());
    }

    private QuizQuestionDisplayDto toDisplayDto(QuizQuestionDisplay d) {
        if (d == null) {
            return null;
        }
        return new QuizQuestionDisplayDto(
                d.getPrompt(),
                d.getSubtitle(),
                d.getInputPlaceholder(),
                d.getTimed(),
                d.getTimeLimitMs());
    }

    private QuizFollowUpDto toFollowUpDto(QuizFollowUp f) {
        if (f == null) {
            return null;
        }
        return new QuizFollowUpDto(f.getStrategy(), f.getReason());
    }
}
