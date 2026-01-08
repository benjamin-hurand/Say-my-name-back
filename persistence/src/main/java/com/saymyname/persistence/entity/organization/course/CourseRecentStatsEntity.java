package com.saymyname.persistence.entity.organization.course;

import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.persistence.multitenancy.BaseOrgScoped;

import jakarta.persistence.*;

@Entity
@Table(name = "course_recent_stats", uniqueConstraints = {
        @UniqueConstraint(name = "uq_crs", columnNames = { "organization_id", "course_id" })
}, indexes = {
        @Index(name = "idx_crs_course", columnList = "organization_id, course_id")
})
public class CourseRecentStatsEntity extends BaseOrgScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", referencedColumnName = "id", nullable = false)
    private CourseEntity course;

    @Column(name = "error_streak", nullable = false)
    private int errorStreak;

    @Column(name = "help_streak", nullable = false)
    private int helpStreak;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_format", length = 32)
    private QuizFormat lastFormat;

    @Column(name = "format_streak", nullable = false)
    private int formatStreak;

    @Column(name = "avg_rt_recent", nullable = false)
    private double avgRtRecent;

    @Column(name = "last_answer_at")
    private LocalDateTime lastAnswerAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public CourseRecentStatsEntity() {
    }

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

    public int getErrorStreak() {
        return errorStreak;
    }

    public void setErrorStreak(int errorStreak) {
        this.errorStreak = errorStreak;
    }

    public int getHelpStreak() {
        return helpStreak;
    }

    public void setHelpStreak(int helpStreak) {
        this.helpStreak = helpStreak;
    }

    public QuizFormat getLastFormat() {
        return lastFormat;
    }

    public void setLastFormat(QuizFormat lastFormat) {
        this.lastFormat = lastFormat;
    }

    public int getFormatStreak() {
        return formatStreak;
    }

    public void setFormatStreak(int formatStreak) {
        this.formatStreak = formatStreak;
    }

    public double getAvgRtRecent() {
        return avgRtRecent;
    }

    public void setAvgRtRecent(double avgRtRecent) {
        this.avgRtRecent = avgRtRecent;
    }

    public LocalDateTime getLastAnswerAt() {
        return lastAnswerAt;
    }

    public void setLastAnswerAt(LocalDateTime lastAnswerAt) {
        this.lastAnswerAt = lastAnswerAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CourseRecentStatsEntity))
            return false;
        CourseRecentStatsEntity that = (CourseRecentStatsEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CourseRecentStatsEntity{" +
                "id=" + id +
                ", courseId=" + (course != null ? course.getId() : null) +
                ", errorStreak=" + errorStreak +
                ", helpStreak=" + helpStreak +
                ", lastFormat=" + lastFormat +
                ", formatStreak=" + formatStreak +
                ", avgRtRecent=" + avgRtRecent +
                ", lastAnswerAt=" + lastAnswerAt +
                '}';
    }
}
