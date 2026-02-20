package com.saymyname.persistence.entity.organization.course;

import com.saymyname.core.model.enums.PoolType;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "course_question_attempts", uniqueConstraints = @UniqueConstraint(name = "uq_cqa_tenant_id", columnNames = {
        "tenant_id", "id" }), indexes = {
                @Index(name = "idx_cqh_course_round", columnList = "course_id,question_round"),
                @Index(name = "idx_cqh_course_answered", columnList = "course_id,answered_at"),
                @Index(name = "idx_cqh_answered_at", columnList = "answered_at"),
                @Index(name = "idx_cqh_course_correct", columnList = "course_id,global_correct"),
                @Index(name = "idx_cqa_tenant_course_round", columnList = "tenant_id,course_id,question_round"),
                @Index(name = "idx_cqa_tenant_answered", columnList = "tenant_id,answered_at")
        })
public class CourseQuestionAttemptEntity extends BaseTenantScoped {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * FK DB: (tenant_id, course_id) -> courses(tenant_id, id)
     * tenant_id est porté par BaseTenantScoped -> join sur tenant_id
     * non-insertable/updatable.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "course_id", referencedColumnName = "id", nullable = false)
    })
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
    @Column(name = "raw_submission", columnDefinition = "longtext")
    private String rawSubmission;

    @Lob
    @Column(name = "normalized_audit", columnDefinition = "longtext")
    private String normalizedAudit;

    @Column(name = "global_correct", nullable = false)
    private boolean globalCorrect;

    @Enumerated(EnumType.STRING)
    @Column(name = "pool_type", nullable = false, length = 255)
    private PoolType poolType;

    @Column(name = "help_used", nullable = false)
    private boolean helpUsed;

    // ----- Snapshot metadata -----
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
    @Column(name = "question_snapshot_json", nullable = false, columnDefinition = "longtext")
    private String questionSnapshotJson;

    @Lob
    @Column(name = "step_state_json", columnDefinition = "longtext")
    private String stepStateJson;

    // ----- Plan fields -----
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
    @Column(name = "planned_target_knowledge_ids_json", columnDefinition = "longtext")
    private String plannedTargetKnowledgeIdsJson;

    @Lob
    @Column(name = "planned_params_json", columnDefinition = "longtext")
    private String plannedParamsJson;

    @Column(name = "planned_reason_code", length = 64)
    private String plannedReasonCode;

    @Lob
    @Column(name = "planned_reason_details_json", columnDefinition = "longtext")
    private String plannedReasonDetailsJson;

    // ✅ Attempt -> Items (aggregate)
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    @BatchSize(size = 100)
    @Builder.Default
    private List<CourseQuestionItemEntity> items = new ArrayList<>();

    public void addItem(CourseQuestionItemEntity item) {
        if (item == null)
            return;
        if (this.items == null)
            this.items = new ArrayList<>();
        this.items.add(item);
        item.setAttempt(this);
    }

    public void removeItem(CourseQuestionItemEntity item) {
        if (item == null)
            return;
        this.items.remove(item);
        item.setAttempt(null);
    }
}
