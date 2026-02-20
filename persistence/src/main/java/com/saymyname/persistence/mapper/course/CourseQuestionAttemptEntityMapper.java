// src/main/java/com/saymyname/persistence/mapper/course/CourseQuestionAttemptEntityMapper.java
package com.saymyname.persistence.mapper.course;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saymyname.core.model.course.CourseQuestionAttempt;
import com.saymyname.core.model.course.CourseQuestionItem;
import com.saymyname.core.model.course.CourseQuestionPlan;
import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.persistence.entity.organization.course.CourseEntity;
import com.saymyname.persistence.entity.organization.course.CourseQuestionAttemptEntity;
import com.saymyname.persistence.entity.organization.course.CourseQuestionItemEntity;

@Component
public class CourseQuestionAttemptEntityMapper {

    /**
     * IMPORTANT:
     * - DB columns are DATETIME (no timezone).
     * - We choose a stable convention for mapping to Instant: UTC.
     * - Do NOT use ZoneId.systemDefault() to avoid environment-dependent bugs.
     */
    private static final ZoneId DB_ZONE = ZoneId.of("UTC");

    private final CourseEntityMapper courseMapper; // kept for DI compatibility (may be used later)
    private final CourseQuestionItemEntityMapper itemMapper;
    private final ObjectMapper objectMapper;

    public CourseQuestionAttemptEntityMapper(
            CourseEntityMapper courseMapper,
            CourseQuestionItemEntityMapper itemMapper,
            ObjectMapper objectMapper) {
        this.courseMapper = Objects.requireNonNull(courseMapper, "courseMapper");
        this.itemMapper = Objects.requireNonNull(itemMapper, "itemMapper");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    public CourseQuestionAttemptEntity toEntity(CourseQuestionAttempt model) {
        if (model == null) {
            return null;
        }

        if (model.getSnapshot() == null) {
            throw new IllegalStateException("CourseQuestionAttempt.snapshot is required for persistence"
                    + " attemptId=" + model.getId()
                    + " courseId=" + model.getCourseId());
        }

        // Strict validation: snapshot must be valid at persistence time.
        try {
            model.getSnapshot().validateInvariants();
        } catch (RuntimeException e) {
            throw new IllegalStateException("Invalid QuizQuestionSnapshot for persistence"
                    + " attemptId=" + model.getId()
                    + " courseId=" + model.getCourseId()
                    + " format=" + model.getSnapshot().getFormat(), e);
        }

        CourseQuestionPlan plan = model.getPlan();
        if (plan == null) {
            throw new IllegalStateException("CourseQuestionAttempt.plan is required for persistence"
                    + " attemptId=" + model.getId()
                    + " courseId=" + model.getCourseId());
        }

        try {
            plan.validateInvariants();
        } catch (RuntimeException e) {
            throw new IllegalStateException("Invalid CourseQuestionPlan for persistence"
                    + " attemptId=" + model.getId()
                    + " courseId=" + model.getCourseId()
                    + " plannedFormat=" + plan.getFormat()
                    + " targetCount=" + plan.getTargetCount(), e);
        }

        CourseQuestionAttemptEntity entity = CourseQuestionAttemptEntity.builder()
                .id(model.getId())
                .course(CourseEntity.builder().id(model.getCourseId()).build())
                .questionRound(model.getQuestionRound())
                .askedAt(model.getAskedAt() != null ? LocalDateTime.ofInstant(model.getAskedAt(), DB_ZONE) : null)
                .answeredAt(
                        model.getAnsweredAt() != null ? LocalDateTime.ofInstant(model.getAnsweredAt(), DB_ZONE) : null)
                .responseTimeMs(model.getResponseTimeMs())
                .rawSubmission(model.getRawSubmission())
                .normalizedAudit(model.getNormalizedAudit())
                .globalCorrect(model.isGlobalCorrect())
                .poolType(model.getPoolType())
                .helpUsed(model.isHelpUsed())
                .build();

        // Snapshot: champs flat + JSON
        QuizQuestionSnapshot snap = model.getSnapshot();
        entity.setQuestionFormat(snap.getFormat());
        entity.setSnapshotSchemaVersion(snap.getSnapshotSchemaVersion());
        entity.setGeneratorVersion(snap.getGeneratorVersion());
        entity.setNormalizerVersion(snap.getNormalizerVersion());
        entity.setQuestionSnapshotJson(writeSnapshotJson(snap, model));

        // Step state: serialize intermediate state for multi-step formats
        entity.setStepStateJson(writeStepStateJson(snap, model.getId()));

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

    public CourseQuestionAttempt toModel(CourseQuestionAttemptEntity entity) {
        if (entity == null) {
            return null;
        }

        List<CourseQuestionItem> items = new ArrayList<>();
        if (entity.getItems() != null) {
            for (CourseQuestionItemEntity it : entity.getItems()) {
                items.add(itemMapper.toModel(it));
            }
        }

        QuizQuestionSnapshot snapshot = readSnapshotJson(entity.getQuestionSnapshotJson(), entity);

        // Merge step state if present (for multi-step formats) - snapshot is immutable
        // (@Value)
        snapshot = mergeStepStateIntoSnapshot(snapshot, entity.getStepStateJson(), entity);

        // Strict validation: snapshot must be valid at read time.
        try {
            snapshot.validateInvariants();
        } catch (RuntimeException e) {
            throw new IllegalStateException("Invalid QuizQuestionSnapshot loaded from DB"
                    + " attemptId=" + entity.getId()
                    + " courseId=" + (entity.getCourse() != null ? entity.getCourse().getId() : null)
                    + " columnFormat=" + entity.getQuestionFormat()
                    + " jsonFormat=" + (snapshot != null ? snapshot.getFormat() : null), e);
        }

        // Safety: coherence between columns and JSON
        if (entity.getQuestionFormat() != null
                && snapshot != null
                && snapshot.getFormat() != null
                && entity.getQuestionFormat() != snapshot.getFormat()) {
            throw new IllegalStateException("Snapshot format mismatch between columns and JSON"
                    + " attemptId=" + entity.getId()
                    + " courseId=" + (entity.getCourse() != null ? entity.getCourse().getId() : null)
                    + " columnFormat=" + entity.getQuestionFormat()
                    + " jsonFormat=" + snapshot.getFormat());
        }

        QuizDecisionReasonCode resolvedReason = QuizDecisionReasonCode.fromString(entity.getPlannedReasonCode());

        CourseQuestionPlan plan = CourseQuestionPlan.builder()
                .format(entity.getPlannedFormat())
                .timed(entity.isPlannedTimed())
                .timeLimitMs(entity.getPlannedTimeLimitMs())
                .targetCount(entity.getPlannedTargetCount())
                .targetKnowledgeIds(readJsonList(entity.getPlannedTargetKnowledgeIdsJson(), entity))
                .paramsJson(entity.getPlannedParamsJson())
                .reasonCode(resolvedReason)
                .reasonDetailsJson(entity.getPlannedReasonDetailsJson())
                .build();

        try {
            plan.validateInvariants();
        } catch (RuntimeException e) {
            throw new IllegalStateException("Invalid CourseQuestionPlan loaded from DB"
                    + " attemptId=" + entity.getId()
                    + " courseId=" + (entity.getCourse() != null ? entity.getCourse().getId() : null)
                    + " plannedFormat=" + entity.getPlannedFormat()
                    + " targetCount=" + entity.getPlannedTargetCount(), e);
        }

        return CourseQuestionAttempt.builder()
                .id(entity.getId())
                .courseId(entity.getCourse() != null ? entity.getCourse().getId() : null)
                .questionRound(entity.getQuestionRound())
                .askedAt(entity.getAskedAt() != null ? entity.getAskedAt().atZone(DB_ZONE).toInstant() : null)
                .answeredAt(entity.getAnsweredAt() != null ? entity.getAnsweredAt().atZone(DB_ZONE).toInstant() : null)
                .responseTimeMs(entity.getResponseTimeMs())
                .rawSubmission(entity.getRawSubmission())
                .normalizedAudit(entity.getNormalizedAudit())
                .globalCorrect(entity.isGlobalCorrect())
                .poolType(entity.getPoolType())
                .helpUsed(entity.isHelpUsed())
                .snapshot(snapshot)
                .plan(plan)
                .items(items)
                .build();
    }

    private String writeSnapshotJson(QuizQuestionSnapshot snapshot, CourseQuestionAttempt model) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize QuizQuestionSnapshot"
                    + " attemptId=" + (model != null ? model.getId() : null)
                    + " courseId=" + (model != null ? model.getCourseId() : null)
                    + " format=" + (snapshot != null ? snapshot.getFormat() : null), e);
        }
    }

    public String writeStepStateJson(QuizQuestionSnapshot snapshot, Long attemptId) {
        if (snapshot == null || snapshot.getFormat() == null) {
            return null;
        }

        try {
            return switch (snapshot.getFormat()) {
                case HANGMAN -> snapshot.getHangmanState() == null
                        ? null
                        : objectMapper.writeValueAsString(snapshot.getHangmanState());
                case WORD_PUZZLE -> snapshot.getWordPuzzleState() == null
                        ? null
                        : objectMapper.writeValueAsString(snapshot.getWordPuzzleState());
                default -> null;
            };
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize step state"
                    + " attemptId=" + (attemptId != null ? attemptId : null)
                    + " format=" + snapshot.getFormat(), e);
        }
    }

    private String writeJsonList(List<Long> values, CourseQuestionAttempt model) {
        if (values == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize plan target knowledge ids"
                    + " attemptId=" + (model != null ? model.getId() : null)
                    + " courseId=" + (model != null ? model.getCourseId() : null),
                    e);
        }
    }

    private QuizQuestionSnapshot readSnapshotJson(String json, CourseQuestionAttemptEntity entity) {
        if (json == null || json.isBlank()) {
            throw new IllegalStateException("questionSnapshotJson is required but was null/blank"
                    + " attemptId=" + (entity != null ? entity.getId() : null)
                    + " courseId="
                    + (entity != null && entity.getCourse() != null ? entity.getCourse().getId() : null));
        }
        try {
            return objectMapper.readValue(json, QuizQuestionSnapshot.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize QuizQuestionSnapshot"
                    + " attemptId=" + (entity != null ? entity.getId() : null)
                    + " courseId=" + (entity != null && entity.getCourse() != null ? entity.getCourse().getId() : null),
                    e);
        }
    }

    private List<Long> readJsonList(String json, CourseQuestionAttemptEntity entity) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize plan target knowledge ids"
                    + " attemptId=" + (entity != null ? entity.getId() : null)
                    + " courseId=" + (entity != null && entity.getCourse() != null ? entity.getCourse().getId() : null),
                    e);
        }
    }

    /**
     * Snapshot is immutable (@Value). We must merge state by rebuilding a new
     * instance.
     */
    private QuizQuestionSnapshot mergeStepStateIntoSnapshot(
            QuizQuestionSnapshot snapshot,
            String stepStateJson,
            CourseQuestionAttemptEntity entity) {

        if (snapshot == null) {
            return null;
        }
        if (stepStateJson == null || stepStateJson.isBlank() || snapshot.getFormat() == null) {
            return snapshot;
        }

        try {
            return switch (snapshot.getFormat()) {
                case HANGMAN -> {
                    var hangmanState = objectMapper.readValue(
                            stepStateJson,
                            com.saymyname.core.model.quiz.snapshot.HangmanSnapshotState.class);
                    yield snapshot.toBuilder().hangmanState(hangmanState).build();
                }
                case WORD_PUZZLE -> {
                    var wordPuzzleState = objectMapper.readValue(
                            stepStateJson,
                            com.saymyname.core.model.quiz.snapshot.WordPuzzleSnapshotState.class);
                    yield snapshot.toBuilder().wordPuzzleState(wordPuzzleState).build();
                }
                default -> snapshot;
            };
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize step state"
                    + " attemptId=" + (entity != null ? entity.getId() : null)
                    + " courseId=" + (entity != null && entity.getCourse() != null ? entity.getCourse().getId() : null)
                    + " format=" + snapshot.getFormat(), e);
        }
    }
}
