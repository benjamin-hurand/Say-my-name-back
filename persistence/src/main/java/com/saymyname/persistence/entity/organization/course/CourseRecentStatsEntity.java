package com.saymyname.persistence.entity.organization.course;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name = "course_recent_stats", indexes = {
        @Index(name = "idx_crs_tenant_course", columnList = "tenant_id,course_id")
})
public class CourseRecentStatsEntity extends BaseTenantScoped {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "error_streak", nullable = false)
    private int errorStreak;

    @Column(name = "help_streak", nullable = false)
    private int helpStreak;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_format", length = 32)
    private LastFormat lastFormat;

    @Column(name = "format_streak", nullable = false)
    private int formatStreak;

    @Column(name = "avg_rt_recent", nullable = false)
    private double avgRtRecent;

    @Column(name = "last_answer_at")
    private LocalDateTime lastAnswerAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime default current_timestamp")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false,
            columnDefinition = "datetime default current_timestamp on update current_timestamp")
    private LocalDateTime updatedAt;

    public enum LastFormat {
        TEXT_INPUT,
        CLOZE,
        HANGMAN,
        MCQ,
        BINARY_SWIPE,
        ASSOCIATION,
        ORDERING
    }
}