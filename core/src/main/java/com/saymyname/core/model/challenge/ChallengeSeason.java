package com.saymyname.core.model.challenge;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Représente une saison compétitive.
 */
public class ChallengeSeason {
    private long id;
    private int seasonNumber;
    private LocalDate startDate;
    private LocalDate endDate;

    // Constructeur par défaut
    public ChallengeSeason() {}

    private ChallengeSeason(Builder builder) {
        this.id = builder.id;
        this.seasonNumber = builder.seasonNumber;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
    }

    // Getters
    public long getId() {
        return id;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    // Builder
    public static class Builder {
        private long id;
        private int seasonNumber;
        private LocalDate startDate;
        private LocalDate endDate;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withSeasonNumber(int seasonNumber) {
            this.seasonNumber = seasonNumber;
            return this;
        }

        public Builder withStartDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder withEndDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public ChallengeSeason build() {
            return new ChallengeSeason(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChallengeSeason that = (ChallengeSeason) o;
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
        return "ChallengeSeason{" +
               "id=" + id +
               ", seasonNumber=" + seasonNumber +
               ", startDate=" + startDate +
               ", endDate=" + endDate +
               '}';
    }
}
