package com.saymyname.persistence.entity.organization.course;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.FactEntity;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "knowledge_stats", indexes = {
        @Index(name = "idx_ks_select", columnList = "tenant_id,user_id,fact_id,error_streak,avg_rt_recent,last_answer_at")
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
    @JoinColumn(name = "fact_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ks_fact"))
    private FactEntity fact;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "knowledge_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ks_knowledge"))
    private KnowledgeEntity knowledge;

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

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime default current_timestamp")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false,
            columnDefinition = "datetime default current_timestamp on update current_timestamp")
    private LocalDateTime updatedAt;
}