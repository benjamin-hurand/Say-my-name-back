// src/main/java/com/saymyname/webapp/mapper/quiz/QuizQuestionDtoMapper.java
package com.saymyname.webapp.mapper.quiz;

import java.util.List;

import org.springframework.stereotype.Component;

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

        String photoUrl = photoUrlResolver.smallUrl(q.getStorageKey());

        QuizQuestionContextDto ctxDto = toContextDto(q.getContext());
        QuizQuestionPayloadDto payloadDto = toPayloadDto(q.getPayload());
        QuizQuestionHintsDto hintsDto = toHintsDto(q.getHints());
        QuizQuestionDisplayDto displayDto = toDisplayDto(q.getDisplay());
        QuizFollowUpDto followUpDto = toFollowUpDto(q.getFollowUp());

        return new QuizQuestionDto(
                q.getQuestionToken(), // ✅ NEW
                q.getPersonId(),
                photoUrl,
                q.getGameModeId(),
                safeList(q.getTargetAttributeIds()),
                q.getOperator(),
                ctxDto,
                q.getFormat(),
                payloadDto,
                hintsDto,
                displayDto,
                followUpDto);
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
                ctx.getDifficultyLevel(), // ok si null (course history n’en a pas)
                ctx.getReducedOptionsId());
    }

    private QuizQuestionPayloadDto toPayloadDto(QuizQuestionPayload p) {
        if (p == null)
            return null;

        List<ChoiceDto> choices = null;
        if (p.getChoices() != null) {
            choices = p.getChoices().stream()
                    .map(c -> new ChoiceDto(c.getId(), c.getLabel(), c.getValue(), c.getPersonId()))
                    .toList();
        }

        ChoiceDto proposition = null;
        if (p.getProposition() != null) {
            var c = p.getProposition();
            proposition = new ChoiceDto(c.getId(), c.getLabel(), c.getValue(), c.getPersonId());
        }

        List<QuizQuestionPayloadDto.ItemDto> items = null;
        if (p.getItems() != null) {
            items = p.getItems().stream()
                    .map(it -> new QuizQuestionPayloadDto.ItemDto(
                            it.getPersonId(),
                            photoUrlResolver.smallUrl(it.getStorageKey()),
                            it.getLabelId()))
                    .toList();
        }

        return new QuizQuestionPayloadDto(
                p.getType(),
                p.getMask(),
                p.getMaxErrors(),
                choices,
                p.getAllowMultiple(),
                proposition,
                items,
                p.getOrderBy());
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

    private static <T> List<T> safeList(List<T> v) {
        return v == null ? List.of() : v;
    }
}
