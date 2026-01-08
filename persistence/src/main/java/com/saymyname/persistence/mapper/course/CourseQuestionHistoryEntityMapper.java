// src/main/java/com/saymyname/persistence/mapper/course/CourseQuestionHistoryEntityMapper.java
package com.saymyname.persistence.mapper.course;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.saymyname.core.model.course.CourseQuestionHistory;
import com.saymyname.core.model.course.CourseQuestionItem;
import com.saymyname.core.model.course.CourseQuestionPlan;
import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;

import com.saymyname.persistence.entity.organization.course.CourseQuestionHistoryEntity;
import com.saymyname.persistence.entity.organization.course.CourseQuestionItemEntity;

@Component
public class CourseQuestionHistoryEntityMapper {

    private final CourseEntityMapper courseMapper;
    private final CourseQuestionItemEntityMapper itemMapper;
    private final ObjectMapper objectMapper;

    public CourseQuestionHistoryEntityMapper(
            CourseEntityMapper courseMapper,
            CourseQuestionItemEntityMapper itemMapper,
            ObjectMapper objectMapper) {
        this.courseMapper = courseMapper;
        this.itemMapper = itemMapper;
        this.objectMapper = objectMapper;
    }

    public CourseQuestionHistoryEntity toEntity(CourseQuestionHistory model) {
        if (model == null)
            return null;

        if (model.getSnapshot() == null) {
            throw new IllegalStateException("CourseQuestionHistory.snapshot is required for persistence");
        }
        model.getSnapshot().validateInvariants();

        CourseQuestionHistoryEntity entity = new CourseQuestionHistoryEntity();
        entity.setId(model.getId());
        entity.setCourse(courseMapper.toEntity(model.getCourse()));
        entity.setQuestionRound(model.getQuestionRound());
        entity.setAskedAt(model.getAskedAt());
        entity.setAnsweredAt(model.getAnsweredAt());
        entity.setResponseTimeMs(model.getResponseTimeMs());
        entity.setRawSubmission(model.getRawSubmission());
        entity.setNormalizedSubmission(model.getNormalizedSubmission());
        entity.setGlobalCorrect(model.isGlobalCorrect());
        entity.setPoolType(model.getPoolType());
        entity.setHelpUsed(model.isHelpUsed());

        // Snapshot: champs flat + JSON
        QuizQuestionSnapshot snap = model.getSnapshot();
        entity.setQuestionFormat(snap.getFormat());
        entity.setSnapshotSchemaVersion(snap.getSnapshotSchemaVersion());
        entity.setGeneratorVersion(snap.getGeneratorVersion());
        entity.setNormalizerVersion(snap.getNormalizerVersion());
        entity.setQuestionSnapshotJson(writeSnapshotJson(snap));

        CourseQuestionPlan plan = model.getPlan();
        if (plan == null) {
            throw new IllegalStateException("CourseQuestionHistory.plan is required for persistence");
        }
        plan.validateInvariants();

        entity.setPlannedFormat(plan.getFormat());
        entity.setPlannedTimed(plan.isTimed());
        entity.setPlannedTimeLimitMs(plan.getTimeLimitMs());
        entity.setPlannedTargetCount(plan.getTargetCount());
        entity.setPlannedTargetKnowledgeIdsJson(writeJsonList(plan.getTargetKnowledgeIds()));
        entity.setPlannedParamsJson(plan.getParamsJson());
        entity.setPlannedReasonCode(plan.getReasonCode() != null ? plan.getReasonCode().name() : null);
        entity.setPlannedReasonDetailsJson(plan.getReasonDetailsJson());
        entity.setPlannedReason(plan.getReasonCode() != null ? plan.getReasonCode().name() : null);

        // Items: maintenir relation bidirectionnelle
        entity.getItems().clear();
        List<CourseQuestionItem> items = model.getItems() != null ? model.getItems() : List.of();
        for (CourseQuestionItem it : items) {
            CourseQuestionItemEntity itEntity = itemMapper.toEntity(it);
            entity.addItem(itEntity);
        }

        return entity;
    }

    public CourseQuestionHistory toModel(CourseQuestionHistoryEntity entity) {
        if (entity == null)
            return null;

        List<CourseQuestionItem> items = new ArrayList<>();
        if (entity.getItems() != null) {
            for (CourseQuestionItemEntity it : entity.getItems()) {
                items.add(itemMapper.toModel(it));
            }
        }

        QuizQuestionSnapshot snapshot = readSnapshotJson(entity.getQuestionSnapshotJson());
        String reasonCode = entity.getPlannedReasonCode() != null
                ? entity.getPlannedReasonCode()
                : entity.getPlannedReason();
        QuizDecisionReasonCode resolvedReason = QuizDecisionReasonCode.fromString(reasonCode);

        CourseQuestionPlan plan = new CourseQuestionPlan.Builder()
                .withFormat(entity.getPlannedFormat())
                .withTimed(entity.isPlannedTimed())
                .withTimeLimitMs(entity.getPlannedTimeLimitMs())
                .withTargetCount(entity.getPlannedTargetCount())
                .withTargetKnowledgeIds(readJsonList(entity.getPlannedTargetKnowledgeIdsJson()))
                .withParamsJson(entity.getPlannedParamsJson())
                .withReasonCode(resolvedReason)
                .withReasonDetailsJson(entity.getPlannedReasonDetailsJson())
                .build();

        // Filet de sécurité : cohérence format/version entre colonnes et JSON
        // (on ne force pas, mais on peut vérifier / log si tu veux)
        if (snapshot != null) {
            if (entity.getQuestionFormat() != snapshot.getFormat()) {
                throw new IllegalStateException("Snapshot format mismatch between columns and JSON");
            }
        }

        return new CourseQuestionHistory.Builder()
                .withId(entity.getId())
                .withCourse(courseMapper.toShortModel(entity.getCourse()))
                .withQuestionRound(entity.getQuestionRound())
                .withAskedAt(entity.getAskedAt())
                .withAnsweredAt(entity.getAnsweredAt())
                .withResponseTimeMs(entity.getResponseTimeMs())
                .withRawSubmission(entity.getRawSubmission())
                .withNormalizedSubmission(entity.getNormalizedSubmission())
                .withGlobalCorrect(entity.isGlobalCorrect())
                .withPoolType(entity.getPoolType())
                .withHelpUsed(entity.isHelpUsed())
                .withSnapshot(snapshot)
                .withPlan(plan)
                .withItems(items)
                .build();
    }

    private String writeSnapshotJson(QuizQuestionSnapshot snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize QuizQuestionSnapshot", e);
        }
    }

    private String writeJsonList(List<Long> values) {
        if (values == null)
            return null;
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize plan target knowledge ids", e);
        }
    }

    private QuizQuestionSnapshot readSnapshotJson(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalStateException("questionSnapshotJson is required but was null/blank");
        }
        try {
            return objectMapper.readValue(json, QuizQuestionSnapshot.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize QuizQuestionSnapshot", e);
        }
    }

    private List<Long> readJsonList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize plan target knowledge ids", e);
        }
    }
}
