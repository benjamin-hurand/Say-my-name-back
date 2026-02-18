package com.saymyname.persistence.entity.organization.course;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import com.saymyname.core.model.enums.course.QuizQuestionItemRole;
import com.saymyname.persistence.entity.organization.course.KnowledgeEntity;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "course_question_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cqi_history_position", columnNames = {"attempt_id", "position"})
}, indexes = {
        @Index(name = "idx_cqi_history", columnList = "attempt_id"),
        @Index(name = "idx_cqi_tenant_attempt", columnList = "tenant_id,attempt_id"),
        @Index(name = "idx_cqi_tenant_person", columnList = "tenant_id,person_id"),
        @Index(name = "idx_cqi_tenant_knowledge", columnList = "tenant_id,knowledge_id")
})
public class CourseQuestionItemEntity extends BaseTenantScoped {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "attempt_id", nullable = false)
    private Long attemptId;

    @Column(name = "position", nullable = false)
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 255)
    private QuizQuestionItemRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "knowledge_id", foreignKey = @ForeignKey(name = "fk_cqi_knowledge"))
    private KnowledgeEntity knowledge;

    @Column(name = "person_id")
    private Long personId;

    @Column(name = "answered", nullable = false)
    private boolean answered;

    @Column(name = "correct")
    private Boolean correct;

    @Lob
    @Column(name = "normalized_answer", columnDefinition = "longtext")
    private String normalizedAnswer;
}
