// src/main/java/com/saymyname/persistence/mapper/course/CourseQuestionAttemptEntityMapper.java
package com.saymyname.persistence.mapper.course;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saymyname.core.model.course.CourseQuestionAttempt;
import com.saymyname.core.model.enums.PoolType;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.persistence.entity.organization.course.CourseQuestionAttemptEntity;

@Component
public class CourseQuestionAttemptEntityMapper {

    private final ObjectMapper objectMapper;

    public CourseQuestionAttemptEntityMapper(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    public CourseQuestionAttemptEntity toEntity(CourseQuestionAttempt model) {
        if (model == null)
            return null;

        CourseQuestionAttemptEntity entity = CourseQuestionAttemptEntity.builder().build();
        entity.setId(model.getId());
        entity.setCourseId(model.getCourseId());
        entity.setQuestionRound(model.getQuestionRound());
        entity.setAskedAt(toLocalDateTime(model.getAskedAt()));
        entity.setAnsweredAt(toLocalDateTime(model.getAnsweredAt()));
        entity.setResponseTimeMs(model.getResponseTimeMs());
        entity.setRawSubmission(model.getRawSubmission());
        entity.setNormalizedAudit(model.getNormalizedAudit());
        entity.setGlobalCorrect(model.isGlobalCorrect());
        entity.setPoolType(toEntityPoolType(model.getPoolType()));
        entity.setHelpUsed(model.isHelpUsed());

        entity.setQuestionFormat(toEntityQuestionFormat(model.getQuestionFormat()));
        entity.setSnapshotSchemaVersion(model.getSnapshotSchemaVersion());
        entity.setGeneratorVersion(model.getGeneratorVersion());
        entity.setNormalizerVersion(model.getNormalizerVersion());
        entity.setQuestionSnapshotJson(model.getQuestionSnapshotJson());
        entity.setStepStateJson(model.getStepStateJson());

        entity.setPlannedFormat(toEntityPlannedFormat(model.getPlannedFormat()));
        entity.setPlannedTimed(model.isPlannedTimed());
        entity.setPlannedTimeLimitMs(model.getPlannedTimeLimitMs());
        entity.setPlannedTargetCount(model.getPlannedTargetCount());
        entity.setPlannedTargetKnowledgeIdsJson(model.getPlannedTargetKnowledgeIdsJson());
        entity.setPlannedParamsJson(model.getPlannedParamsJson());
        entity.setPlannedReasonCode(model.getPlannedReasonCode());
        entity.setPlannedReasonDetailsJson(model.getPlannedReasonDetailsJson());

        return entity;
    }

    public CourseQuestionAttempt toModel(CourseQuestionAttemptEntity entity) {
        if (entity == null)
            return null;

        return CourseQuestionAttempt.builder()
                .id(entity.getId())
                .courseId(entity.getCourseId())
                .questionRound(entity.getQuestionRound())
                .askedAt(toInstant(entity.getAskedAt()))
                .answeredAt(toInstant(entity.getAnsweredAt()))
                .responseTimeMs(entity.getResponseTimeMs())
                .rawSubmission(entity.getRawSubmission())
                .normalizedAudit(entity.getNormalizedAudit())
                .globalCorrect(entity.isGlobalCorrect())
                .poolType(toModelPoolType(entity.getPoolType()))
                .helpUsed(entity.isHelpUsed())
                .questionFormat(toModelQuestionFormat(entity.getQuestionFormat()))
                .snapshotSchemaVersion(entity.getSnapshotSchemaVersion())
                .generatorVersion(entity.getGeneratorVersion())
                .normalizerVersion(entity.getNormalizerVersion())
                .questionSnapshotJson(entity.getQuestionSnapshotJson())
                .stepStateJson(entity.getStepStateJson())
                .plannedFormat(toModelPlannedFormat(entity.getPlannedFormat()))
                .plannedTimed(entity.isPlannedTimed())
                .plannedTimeLimitMs(entity.getPlannedTimeLimitMs())
                .plannedTargetCount(entity.getPlannedTargetCount())
                .plannedTargetKnowledgeIdsJson(entity.getPlannedTargetKnowledgeIdsJson())
                .plannedParamsJson(entity.getPlannedParamsJson())
                .plannedReasonCode(entity.getPlannedReasonCode())
                .plannedReasonDetailsJson(entity.getPlannedReasonDetailsJson())
                .build();
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

    private CourseQuestionAttemptEntity.PoolType toEntityPoolType(PoolType value) {
        return value == null ? null : CourseQuestionAttemptEntity.PoolType.valueOf(value.name());
    }

    private PoolType toModelPoolType(CourseQuestionAttemptEntity.PoolType value) {
        return value == null ? null : PoolType.valueOf(value.name());
    }

    private CourseQuestionAttemptEntity.QuestionFormat toEntityQuestionFormat(CourseQuestionAttempt.QuestionFormat value) {
        return value == null ? null : CourseQuestionAttemptEntity.QuestionFormat.valueOf(value.name());
    }

    private CourseQuestionAttempt.QuestionFormat toModelQuestionFormat(CourseQuestionAttemptEntity.QuestionFormat value) {
        return value == null ? null : CourseQuestionAttempt.QuestionFormat.valueOf(value.name());
    }

    private CourseQuestionAttemptEntity.PlannedFormat toEntityPlannedFormat(CourseQuestionAttempt.PlannedFormat value) {
        return value == null ? null : CourseQuestionAttemptEntity.PlannedFormat.valueOf(value.name());
    }

    private CourseQuestionAttempt.PlannedFormat toModelPlannedFormat(CourseQuestionAttemptEntity.PlannedFormat value) {
        return value == null ? null : CourseQuestionAttempt.PlannedFormat.valueOf(value.name());
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
