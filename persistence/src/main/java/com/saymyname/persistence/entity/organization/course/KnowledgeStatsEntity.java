package com.saymyname.persistence.entity.organization.course;

import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.FactEntity;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "knowledge_stats", indexes = {
                @Index(name = "idx_ks_select", columnList = "tenant_id,user_id,fact_id,error_streak,avg_rt_recent,last_answer_at"),
                @Index(name = "fk_ks_user", columnList = "user_id"),
                @Index(name = "fk_ks_fact", columnList = "fact_id"),
                @Index(name = "fk_ks_knowledge", columnList = "knowledge_id")
})
public class KnowledgeStatsEntity extends BaseTenantScoped {

        @EqualsAndHashCode.Include
        @ToString.Include
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ks_user"))
        private UserEntity user;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumns({
                        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false),
                        @JoinColumn(name = "fact_id", referencedColumnName = "id", insertable = false, updatable = false)
        })
        private FactEntity fact;

        @Column(name = "fact_id", nullable = false)
        private Long factId;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumns({
                        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false),
                        @JoinColumn(name = "knowledge_id", referencedColumnName = "id", insertable = false, updatable = false)
        })
        private KnowledgeEntity knowledge;

        @Column(name = "knowledge_id", nullable = false)
        private Long knowledgeId;

        @Column(name = "attempts_recent", nullable = false)
        private double attemptsRecent;

        @Column(name = "correct_recent", nullable = false)
        private double correctRecent;

        @Column(name = "help_recent", nullable = false)
        private double helpRecent;

        @Column(name = "avg_rt_recent", nullable = false)
        private double avgRtRecent;

        @Column(name = "last_answer_at")
        private LocalDateTime lastAnswerAt;

        @Column(name = "last_correct")
        private Boolean lastCorrect;

        @Column(name = "last_help_used")
        private Boolean lastHelpUsed;

        @Column(name = "last_response_time_ms", nullable = false)
        private int lastResponseTimeMs;

        @Column(name = "error_streak", nullable = false)
        private int errorStreak;

        /**
         * Laisse MySQL gérer DEFAULT CURRENT_TIMESTAMP et ON UPDATE CURRENT_TIMESTAMP
         */
        @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
        private LocalDateTime createdAt;

        @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
        private LocalDateTime updatedAt;
}