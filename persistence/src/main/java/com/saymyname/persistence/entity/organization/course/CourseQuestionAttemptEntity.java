// src/main/java/com/saymyname/persistence/entity/organization/course/CourseQuestionAttemptEntity.java
package com.saymyname.persistence.entity.organization.course;

import com.saymyname.core.model.enums.PoolType;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.persistence.multitenancy.BaseOrgScoped;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "course_question_attempts", indexes = {
        @Index(name = "idx_cqh_course_round", columnList = "course_id,question_round"),
        @Index(name = "idx_cqh_course_answered", columnList = "course_id,answered_at"),
        @Index(name = "idx_cqh_answered_at", columnList = "answered_at"),
        @Index(name = "idx_cqh_course_correct", columnList = "course_id,global_correct"),
        @Index(name = "idx_cqh_org", columnList = "organization_id"),
        @Index(name = "idx_cqh_course_org", columnList = "course_id,organization_id")
})
public class CourseQuestionAttemptEntity extends BaseOrgScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", referencedColumnName = "id", nullable = false)
    private CourseEntity course;

    @Column(name = "question_round", nullable = false)
    private int questionRound;

    @Column(name = "asked_at", nullable = false)
    private LocalDateTime askedAt;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Column(name = "response_time_ms", nullable = false)
    private int responseTimeMs;

    @Lob
    @Column(name = "raw_submission")
    private String rawSubmission;

    @Lob
    @Column(name = "normalized_audit")
    private String normalizedAudit;

    @Column(name = "global_correct", nullable = false)
    private boolean globalCorrect;

    @Enumerated(EnumType.STRING)
    @Column(name = "pool_type", nullable = false)
    private PoolType poolType;

    @Column(name = "help_used", nullable = false)
    private boolean helpUsed;

    // -----------------------------
    // Snapshot fields (Option B)
    // -----------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "question_format", nullable = false, length = 32)
    private QuizFormat questionFormat;

    @Column(name = "snapshot_schema_version", nullable = false)
    private int snapshotSchemaVersion;

    @Column(name = "generator_version", nullable = false, length = 64)
    private String generatorVersion;

    @Column(name = "normalizer_version", nullable = false, length = 64)
    private String normalizerVersion;

    @Lob
    @Column(name = "question_snapshot_json", nullable = false, columnDefinition = "LONGTEXT")
    private String questionSnapshotJson;

    /**
     * Multi-step state (for HANGMAN/WORD_PUZZLE formats).
     * Stores intermediate state between submissions (mask, attempts, feedback).
     * Deserialized and merged into snapshot on read.
     */
    @Lob
    @Column(name = "step_state_json", columnDefinition = "LONGTEXT")
    private String stepStateJson;

    // -----------------------------
    // Plan fields
    // -----------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "planned_format", nullable = false, length = 32)
    private QuizFormat plannedFormat;

    @Column(name = "planned_timed", nullable = false)
    private boolean plannedTimed;

    @Column(name = "planned_time_limit_ms")
    private Integer plannedTimeLimitMs;

    @Column(name = "planned_target_count", nullable = false)
    private int plannedTargetCount;

    @Lob
    @Column(name = "planned_target_knowledge_ids_json", columnDefinition = "LONGTEXT")
    private String plannedTargetKnowledgeIdsJson;

    @Lob
    @Column(name = "planned_params_json", columnDefinition = "LONGTEXT")
    private String plannedParamsJson;

    @Column(name = "planned_reason_code", length = 64)
    private String plannedReasonCode;

    @Lob
    @Column(name = "planned_reason_details_json", columnDefinition = "LONGTEXT")
    private String plannedReasonDetailsJson;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @BatchSize(size = 100)
    private List<CourseQuestionItemEntity> items = new ArrayList<>();

    public CourseQuestionAttemptEntity() {
    }

    public void addItem(CourseQuestionItemEntity item) {
        items.add(item);
        item.setAttempt(this);
    }

    public void removeItem(CourseQuestionItemEntity item) {
        items.remove(item);
        item.setAttempt(null);
    }

    // getters/setters (ajoute ceux des nouveaux champs)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CourseEntity getCourse() {
        return course;
    }

    public void setCourse(CourseEntity course) {
        this.course = course;
    }

    public int getQuestionRound() {
        return questionRound;
    }

    public void setQuestionRound(int questionRound) {
        this.questionRound = questionRound;
    }

    public LocalDateTime getAskedAt() {
        return askedAt;
    }

    public void setAskedAt(LocalDateTime askedAt) {
        this.askedAt = askedAt;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }

    public int getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(int responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getRawSubmission() {
        return rawSubmission;
    }

    public void setRawSubmission(String rawSubmission) {
        this.rawSubmission = rawSubmission;
    }

    public String getNormalizedAudit() {
        return normalizedAudit;
    }

    public void setNormalizedAudit(String normalizedAudit) {
        this.normalizedAudit = normalizedAudit;
    }

    public boolean isGlobalCorrect() {
        return globalCorrect;
    }

    public void setGlobalCorrect(boolean globalCorrect) {
        this.globalCorrect = globalCorrect;
    }

    public PoolType getPoolType() {
        return poolType;
    }

    public void setPoolType(PoolType poolType) {
        this.poolType = poolType;
    }

    public boolean isHelpUsed() {
        return helpUsed;
    }

    public void setHelpUsed(boolean helpUsed) {
        this.helpUsed = helpUsed;
    }

    public QuizFormat getQuestionFormat() {
        return questionFormat;
    }

    public void setQuestionFormat(QuizFormat questionFormat) {
        this.questionFormat = questionFormat;
    }

    public int getSnapshotSchemaVersion() {
        return snapshotSchemaVersion;
    }

    public void setSnapshotSchemaVersion(int snapshotSchemaVersion) {
        this.snapshotSchemaVersion = snapshotSchemaVersion;
    }

    public String getGeneratorVersion() {
        return generatorVersion;
    }

    public void setGeneratorVersion(String generatorVersion) {
        this.generatorVersion = generatorVersion;
    }

    public String getNormalizerVersion() {
        return normalizerVersion;
    }

    public void setNormalizerVersion(String normalizerVersion) {
        this.normalizerVersion = normalizerVersion;
    }

    public String getQuestionSnapshotJson() {
        return questionSnapshotJson;
    }

    public void setQuestionSnapshotJson(String questionSnapshotJson) {
        this.questionSnapshotJson = questionSnapshotJson;
    }

    public String getStepStateJson() {
        return stepStateJson;
    }

    public void setStepStateJson(String stepStateJson) {
        this.stepStateJson = stepStateJson;
    }

    public QuizFormat getPlannedFormat() {
        return plannedFormat;
    }

    public void setPlannedFormat(QuizFormat plannedFormat) {
        this.plannedFormat = plannedFormat;
    }

    public boolean isPlannedTimed() {
        return plannedTimed;
    }

    public void setPlannedTimed(boolean plannedTimed) {
        this.plannedTimed = plannedTimed;
    }

    public Integer getPlannedTimeLimitMs() {
        return plannedTimeLimitMs;
    }

    public void setPlannedTimeLimitMs(Integer plannedTimeLimitMs) {
        this.plannedTimeLimitMs = plannedTimeLimitMs;
    }

    public int getPlannedTargetCount() {
        return plannedTargetCount;
    }

    public void setPlannedTargetCount(int plannedTargetCount) {
        this.plannedTargetCount = plannedTargetCount;
    }

    public String getPlannedTargetKnowledgeIdsJson() {
        return plannedTargetKnowledgeIdsJson;
    }

    public void setPlannedTargetKnowledgeIdsJson(String plannedTargetKnowledgeIdsJson) {
        this.plannedTargetKnowledgeIdsJson = plannedTargetKnowledgeIdsJson;
    }

    public String getPlannedParamsJson() {
        return plannedParamsJson;
    }

    public void setPlannedParamsJson(String plannedParamsJson) {
        this.plannedParamsJson = plannedParamsJson;
    }

    public String getPlannedReasonCode() {
        return plannedReasonCode;
    }

    public void setPlannedReasonCode(String plannedReasonCode) {
        this.plannedReasonCode = plannedReasonCode;
    }

    public String getPlannedReasonDetailsJson() {
        return plannedReasonDetailsJson;
    }

    public void setPlannedReasonDetailsJson(String plannedReasonDetailsJson) {
        this.plannedReasonDetailsJson = plannedReasonDetailsJson;
    }

    public List<CourseQuestionItemEntity> getItems() {
        return items;
    }

    public void setItems(List<CourseQuestionItemEntity> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CourseQuestionAttemptEntity))
            return false;
        CourseQuestionAttemptEntity that = (CourseQuestionAttemptEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
