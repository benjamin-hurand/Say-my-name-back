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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

        /**
         * FK DB: (tenant_id, attempt_id) -> course_question_attempts(tenant_id, id)
         *
         * IMPORTANT:
         * - tenant_id is inherited from BaseTenantScoped and is part of the FK
         * - we set insertable=false/updatable=false on the tenant join column to avoid
         * duplicate column mapping.
         */
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumns({
                        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", nullable = false, insertable = false, updatable = false),
                        @JoinColumn(name = "attempt_id", referencedColumnName = "id", nullable = false)
        })
        private CourseQuestionAttemptEntity attempt;

        @Column(name = "position", nullable = false)
        private int position;

        @Enumerated(EnumType.STRING)
        @Column(name = "role", nullable = false, length = 255)
        private QuizQuestionItemRole role;

        /**
         * FK DB (après patch): (tenant_id, knowledge_id) -> knowledges(tenant_id, id)
         * knowledge_id nullable (SET NULL)
         */
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumns({
                        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", nullable = false, insertable = false, updatable = false),
                        @JoinColumn(name = "knowledge_id", referencedColumnName = "id")
        })
        private KnowledgeEntity knowledge;

        /**
         * FK DB (après patch): (tenant_id, person_id) -> persons(tenant_id, id)
         * person_id nullable
         */
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumns({
                        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", nullable = false, insertable = false, updatable = false),
                        @JoinColumn(name = "person_id", referencedColumnName = "id")
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
