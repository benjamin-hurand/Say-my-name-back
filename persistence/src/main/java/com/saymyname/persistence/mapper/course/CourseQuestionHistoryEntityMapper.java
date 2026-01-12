// src/main/java/com/saymyname/persistence/mapper/course/CourseQuestionHistoryEntityMapper.java
package com.saymyname.persistence.mapper.course;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        this.courseMapper = Objects.requireNonNull(courseMapper, "courseMapper");
        this.itemMapper = Objects.requireNonNull(itemMapper, "itemMapper");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    public CourseQuestionHistoryEntity toEntity(CourseQuestionHistory model) {
        if (model == null)
            return null;

        if (model.getSnapshot() == null) {
            throw new IllegalStateException("CourseQuestionHistory.snapshot is required for persistence"
                    + " historyId=" + model.getId()
                    + " courseId=" + (model.getCourse() != null ? model.getCourse().getId() : null));
        }

        // Strict validation: snapshot must be valid at persistence time.
        try {
            model.getSnapshot().validateInvariants();
        } catch (RuntimeException e) {
            throw new IllegalStateException("Invalid QuizQuestionSnapshot for persistence"
                    + " historyId=" + model.getId()
                    + " courseId=" + (model.getCourse() != null ? model.getCourse().getId() : null)
                    + " format=" + model.getSnapshot().getFormat(), e);
        }

        CourseQuestionPlan plan = model.getPlan();
        if (plan == null) {
            throw new IllegalStateException("CourseQuestionHistory.plan is required for persistence"
                    + " historyId=" + model.getId()
                    + " courseId=" + (model.getCourse() != null ? model.getCourse().getId() : null));
        }

        try {
            plan.validateInvariants();
        } catch (RuntimeException e) {
            throw new IllegalStateException("Invalid CourseQuestionPlan for persistence"
                    + " historyId=" + model.getId()
                    + " courseId=" + (model.getCourse() != null ? model.getCourse().getId() : null)
                    + " plannedFormat=" + plan.getFormat()
                    + " targetCount=" + plan.getTargetCount(), e);
        }

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
        entity.setQuestionSnapshotJson(writeSnapshotJson(snap, model));

        // Plan: champs flat
        entity.setPlannedFormat(plan.getFormat());
        entity.setPlannedTimed(plan.isTimed());
        entity.setPlannedTimeLimitMs(plan.getTimeLimitMs());
        entity.setPlannedTargetCount(plan.getTargetCount());
        entity.setPlannedTargetKnowledgeIdsJson(writeJsonList(plan.getTargetKnowledgeIds(), model));
        entity.setPlannedParamsJson(plan.getParamsJson());
        entity.setPlannedReasonCode(plan.getReasonCode() != null ? plan.getReasonCode().name() : null);
        entity.setPlannedReasonDetailsJson(plan.getReasonDetailsJson());

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

        QuizQuestionSnapshot snapshot = readSnapshotJson(entity.getQuestionSnapshotJson(), entity);

        // Strict validation: snapshot must be valid at read time.
        try {
            snapshot.validateInvariants();
        } catch (RuntimeException e) {
            throw new IllegalStateException("Invalid QuizQuestionSnapshot loaded from DB"
                    + " historyId=" + entity.getId()
                    + " courseId=" + (entity.getCourse() != null ? entity.getCourse().getId() : null)
                    + " columnFormat=" + entity.getQuestionFormat()
                    + " jsonFormat=" + snapshot.getFormat(), e);
        }

        // Safety: coherence between columns and JSON
        if (entity.getQuestionFormat() != null && snapshot.getFormat() != null
                && entity.getQuestionFormat() != snapshot.getFormat()) {
            throw new IllegalStateException("Snapshot format mismatch between columns and JSON"
                    + " historyId=" + entity.getId()
                    + " courseId=" + (entity.getCourse() != null ? entity.getCourse().getId() : null)
                    + " columnFormat=" + entity.getQuestionFormat()
                    + " jsonFormat=" + snapshot.getFormat());
        }

        String reasonCode = entity.getPlannedReasonCode() != null
                ? entity.getPlannedReasonCode()
                : null;
        QuizDecisionReasonCode resolvedReason = QuizDecisionReasonCode.fromString(reasonCode);

        CourseQuestionPlan plan = new CourseQuestionPlan.Builder()
                .withFormat(entity.getPlannedFormat())
                .withTimed(entity.isPlannedTimed())
                .withTimeLimitMs(entity.getPlannedTimeLimitMs())
                .withTargetCount(entity.getPlannedTargetCount())
                .withTargetKnowledgeIds(readJsonList(entity.getPlannedTargetKnowledgeIdsJson(), entity))
                .withParamsJson(entity.getPlannedParamsJson())
                .withReasonCode(resolvedReason)
                .withReasonDetailsJson(entity.getPlannedReasonDetailsJson())
                .build();

        try {
            plan.validateInvariants();
        } catch (RuntimeException e) {
            throw new IllegalStateException("Invalid CourseQuestionPlan loaded from DB"
                    + " historyId=" + entity.getId()
                    + " courseId=" + (entity.getCourse() != null ? entity.getCourse().getId() : null)
                    + " plannedFormat=" + entity.getPlannedFormat()
                    + " targetCount=" + entity.getPlannedTargetCount(), e);
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

    private String writeSnapshotJson(QuizQuestionSnapshot snapshot, CourseQuestionHistory model) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize QuizQuestionSnapshot"
                    + " historyId=" + (model != null ? model.getId() : null)
                    + " courseId=" + (model != null && model.getCourse() != null ? model.getCourse().getId() : null)
                    + " format=" + (snapshot != null ? snapshot.getFormat() : null), e);
        }
    }

    private String writeJsonList(List<Long> values, CourseQuestionHistory model) {
        if (values == null)
            return null;
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize plan target knowledge ids"
                    + " historyId=" + (model != null ? model.getId() : null)
                    + " courseId=" + (model != null && model.getCourse() != null ? model.getCourse().getId() : null),
                    e);
        }
    }

    private QuizQuestionSnapshot readSnapshotJson(String json, CourseQuestionHistoryEntity entity) {
        if (json == null || json.isBlank()) {
            throw new IllegalStateException("questionSnapshotJson is required but was null/blank"
                    + " historyId=" + (entity != null ? entity.getId() : null)
                    + " courseId="
                    + (entity != null && entity.getCourse() != null ? entity.getCourse().getId() : null));
        }
        try {
            return objectMapper.readValue(json, QuizQuestionSnapshot.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize QuizQuestionSnapshot"
                    + " historyId=" + (entity != null ? entity.getId() : null)
                    + " courseId=" + (entity != null && entity.getCourse() != null ? entity.getCourse().getId() : null),
                    e);
        }
    }

    private List<Long> readJsonList(String json, CourseQuestionHistoryEntity entity) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize plan target knowledge ids"
                    + " historyId=" + (entity != null ? entity.getId() : null)
                    + " courseId=" + (entity != null && entity.getCourse() != null ? entity.getCourse().getId() : null),
                    e);
        }
    }
}
