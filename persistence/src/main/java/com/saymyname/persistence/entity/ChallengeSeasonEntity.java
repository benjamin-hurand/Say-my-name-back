package com.saymyname.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "challenge_seasons", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"season_number", "start_date", "end_date"})
})
public class ChallengeSeasonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "season_number", nullable = false)
    private int seasonNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    public ChallengeSeasonEntity() {}

    public ChallengeSeasonEntity(long id, int seasonNumber, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.seasonNumber = seasonNumber;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public int getSeasonNumber() { return seasonNumber; }
    public void setSeasonNumber(int seasonNumber) { this.seasonNumber = seasonNumber; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    // Builder
    public static class Builder {
        private long id;
        private int seasonNumber;
        private LocalDate startDate;
        private LocalDate endDate;

        public Builder withId(long id) { this.id = id; return this; }
        public Builder withSeasonNumber(int seasonNumber) { this.seasonNumber = seasonNumber; return this; }
        public Builder withStartDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public Builder withEndDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public ChallengeSeasonEntity build() {
            return new ChallengeSeasonEntity(id, seasonNumber, startDate, endDate);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChallengeSeasonEntity that = (ChallengeSeasonEntity) o;
        return id == that.id &&
               seasonNumber == that.seasonNumber &&
               Objects.equals(startDate, that.startDate) &&
               Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, seasonNumber, startDate, endDate);
    }

    @Override
    public String toString() {
        return "ChallengeSeasonEntity{" +
               "id=" + id +
               ", seasonNumber=" + seasonNumber +
               ", startDate=" + startDate +
               ", endDate=" + endDate +
               '}';
    }
}
