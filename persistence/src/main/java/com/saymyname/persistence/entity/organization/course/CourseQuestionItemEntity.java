// src/main/java/com/saymyname/persistence/entity/organization/course/CourseQuestionItemEntity.java
package com.saymyname.persistence.entity.organization.course;

import com.saymyname.core.model.enums.course.QuizQuestionItemRole;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "course_question_items", uniqueConstraints = @UniqueConstraint(name = "uk_cqi_history_position", columnNames = {
                "attempt_id", "position" }), indexes = {
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

        @Column(name = "knowledge_id")
        private Long knowledgeId;

        @Column(name = "person_id")
        private Long personId;

        /**
         * FK DB: (tenant_id, attempt_id) -> course_question_attempts(tenant_id, id)
         * Relation read-only : attemptId est le writer.
         */
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumns({
                        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", nullable = false, insertable = false, updatable = false),
                        @JoinColumn(name = "attempt_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
        })
        private CourseQuestionAttemptEntity attempt;

        @Column(name = "position", nullable = false)
        private int position;

        @Enumerated(EnumType.STRING)
        @Column(name = "role", nullable = false, length = 255)
        private QuizQuestionItemRole role;

        /**
         * FK DB: (tenant_id, knowledge_id) -> knowledges(tenant_id, id)
         * knowledge_id nullable (SET NULL). Relation read-only : knowledgeId est le writer.
         * Pas de nullable=false sur tenant_id ici : la relation est optionnelle,
         * toutes les colonnes du @JoinColumns doivent avoir la même nullabilité.
         */
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumns({
                        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false),
                        @JoinColumn(name = "knowledge_id", referencedColumnName = "id", insertable = false, updatable = false)
        })
        private KnowledgeEntity knowledge;

        /**
         * FK DB: (tenant_id, person_id) -> persons(tenant_id, id)
         * person_id nullable. Relation read-only : personId est le writer.
         * Même règle : pas de nullable=false, relation optionnelle.
         */
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumns({
                        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false),
                        @JoinColumn(name = "person_id", referencedColumnName = "id", insertable = false, updatable = false)
        })
        private PersonEntity person;

        @Column(name = "answered", nullable = false)
        private boolean answered;

        @Column(name = "correct")
        private Boolean correct;

        @Lob
        @Column(name = "normalized_answer", columnDefinition = "longtext")
        private String normalizedAnswer;
}
