package com.saymyname.core.model.challenge;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class ChallengeMenu {
    private Long userId;
    private LocalDateTime seasonStart;
    private String search;
    private ChallengeFilters filters;
    private List<ChallengeSortCriterion> sorts;

    public ChallengeMenu() {
    }

    public ChallengeMenu(Long userId, LocalDateTime seasonStart, String search, ChallengeFilters filters,
            List<ChallengeSortCriterion> sorts) {
        this.userId = userId;
        this.seasonStart = seasonStart;
        this.search = search;
        this.filters = filters;
        this.sorts = sorts;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getSeasonStart() {
        return seasonStart;
    }

    public void setSeasonStart(LocalDateTime seasonStart) {
        this.seasonStart = seasonStart;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public ChallengeFilters getFilters() {
        return filters;
    }

    public void setFilters(ChallengeFilters filters) {
        this.filters = filters;
    }

    public List<ChallengeSortCriterion> getSorts() {
        return sorts;
    }

    public void setSorts(List<ChallengeSortCriterion> sorts) {
        this.sorts = sorts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ChallengeMenu))
            return false;
        ChallengeMenu that = (ChallengeMenu) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(seasonStart, that.seasonStart) &&
                Objects.equals(search, that.search) &&
                Objects.equals(filters, that.filters) &&
                Objects.equals(sorts, that.sorts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, seasonStart, search, filters, sorts);
    }

    @Override
    public String toString() {
        return "ChallengeMenu{" +
                "userId=" + userId +
                ", seasonStart=" + seasonStart +
                ", search='" + search + '\'' +
                ", filters=" + filters +
                ", sorts=" + sorts +
                '}';
    }
}
