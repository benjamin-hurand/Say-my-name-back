package com.saymyname.core.model.course;

import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.core.model.enums.quiz.QuizFormat;

public class CourseRecentStats {

    private Long id;
    private Long courseId;
    private int errorStreak;
    private int helpStreak;
    private QuizFormat lastFormat;
    private int formatStreak;
    private double avgRtRecent;
    private LocalDateTime lastAnswerAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CourseRecentStats() {
    }

    private CourseRecentStats(Builder b) {
        this.id = b.id;
        this.courseId = b.courseId;
        this.errorStreak = b.errorStreak;
        this.helpStreak = b.helpStreak;
        this.lastFormat = b.lastFormat;
        this.formatStreak = b.formatStreak;
        this.avgRtRecent = b.avgRtRecent;
        this.lastAnswerAt = b.lastAnswerAt;
        this.createdAt = b.createdAt;
        this.updatedAt = b.updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
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

    public static class Builder {
        private Long id;
        private Long courseId;
        private int errorStreak;
        private int helpStreak;
        private QuizFormat lastFormat;
        private int formatStreak;
        private double avgRtRecent;
        private LocalDateTime lastAnswerAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withCourseId(Long courseId) {
            this.courseId = courseId;
            return this;
        }

        public Builder withErrorStreak(int errorStreak) {
            this.errorStreak = errorStreak;
            return this;
        }

        public Builder withHelpStreak(int helpStreak) {
            this.helpStreak = helpStreak;
            return this;
        }

        public Builder withLastFormat(QuizFormat lastFormat) {
            this.lastFormat = lastFormat;
            return this;
        }

        public Builder withFormatStreak(int formatStreak) {
            this.formatStreak = formatStreak;
            return this;
        }

        public Builder withAvgRtRecent(double avgRtRecent) {
            this.avgRtRecent = avgRtRecent;
            return this;
        }

        public Builder withLastAnswerAt(LocalDateTime lastAnswerAt) {
            this.lastAnswerAt = lastAnswerAt;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public CourseRecentStats build() {
            return new CourseRecentStats(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CourseRecentStats))
            return false;
        CourseRecentStats that = (CourseRecentStats) o;
        return errorStreak == that.errorStreak
                && helpStreak == that.helpStreak
                && formatStreak == that.formatStreak
                && Double.compare(that.avgRtRecent, avgRtRecent) == 0
                && Objects.equals(id, that.id)
                && Objects.equals(courseId, that.courseId)
                && lastFormat == that.lastFormat
                && Objects.equals(lastAnswerAt, that.lastAnswerAt)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, courseId, errorStreak, helpStreak,
                lastFormat, formatStreak, avgRtRecent, lastAnswerAt, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "CourseRecentStats{" +
                "id=" + id +
                ", courseId=" + courseId +
                ", errorStreak=" + errorStreak +
                ", helpStreak=" + helpStreak +
                ", lastFormat=" + lastFormat +
                ", formatStreak=" + formatStreak +
                ", avgRtRecent=" + avgRtRecent +
                ", lastAnswerAt=" + lastAnswerAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
