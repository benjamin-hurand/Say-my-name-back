package com.saymyname.persistence.entity.organization.course;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.FactEntity;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "knowledges", indexes = {
        @Index(name = "idx_k_select", columnList = "tenant_id,user_id,status,next_review_date"),
        @Index(name = "idx_k_fact", columnList = "fact_id")
})
public class KnowledgeEntity extends BaseTenantScoped {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_knowledge_user"))
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fact_id", nullable = false, foreignKey = @ForeignKey(name = "fk_knowledge_fact"))
    private FactEntity fact;

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
}
