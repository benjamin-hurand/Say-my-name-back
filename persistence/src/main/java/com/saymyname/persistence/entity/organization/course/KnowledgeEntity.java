package com.saymyname.persistence.entity.organization.course;

import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.FactEntity;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "knowledges", uniqueConstraints = {
                @UniqueConstraint(name = "uq_k_user_fact", columnNames = { "tenant_id", "user_id", "fact_id" })
}, indexes = {
                @Index(name = "idx_k_select", columnList = "tenant_id,user_id,status,next_review_date"),
                @Index(name = "idx_k_fact", columnList = "fact_id"),
                @Index(name = "idx_k_reval", columnList = "tenant_id,user_id,pending_revalidation,next_review_date"),
                @Index(name = "fk_k_fact_tenant", columnList = "tenant_id,fact_id")
})
public class KnowledgeEntity extends BaseTenantScoped {

        @EqualsAndHashCode.Include
        @ToString.Include
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_k_user"))
        private UserEntity user;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumns({
                        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false),
                        @JoinColumn(name = "fact_id", referencedColumnName = "id", insertable = false, updatable = false)
        })
        private FactEntity fact;

        @Column(name = "fact_id", nullable = false)
        private Long factId;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 10)
        private KnowledgeStatus status;

        @Column(name = "next_review_date", nullable = false)
        private LocalDateTime nextReviewDate;

        @Column(name = "last_review_date")
        private LocalDateTime lastReviewDate;

        @Column(name = "total_repetition_count", nullable = false)
        private int totalRepetitionCount;

        @Column(name = "failure_count", nullable = false)
        private int failureCount;

        @Column(name = "success_count", nullable = false)
        private int successCount;

        @Column(name = "srs_streak", nullable = false)
        private int srsStreak;

        @Column(name = "global_streak", nullable = false)
        private int globalStreak;

        @Column(name = "ease_factor", nullable = false, precision = 10, scale = 2)
        private BigDecimal easeFactor;

        @Column(name = "difficulty", nullable = false)
        private double difficulty;

        @Column(name = "stability", nullable = false)
        private double stability;

        @Column(name = "pending_revalidation", nullable = false)
        private boolean pendingRevalidation;

        @Column(name = "revalidation_reason", length = 64)
        private String revalidationReason;
}
