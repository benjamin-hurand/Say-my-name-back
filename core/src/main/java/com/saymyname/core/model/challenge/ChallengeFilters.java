package com.saymyname.core.model.challenge;

import com.saymyname.core.model.enums.UserPerformance;
import com.saymyname.core.model.game.options.GameAttributeFilter;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Modélise les filtres appliqués lors de la recherche de challenges.
 */
public class ChallengeFilters {
    private List<Long> gameModeIds;
    private List<UserPerformance> userPerformances; // Vous pouvez par la suite remplacer par une enum si besoin.
    private GameAttributeFilter attributeFilter;;
    private Long participantsRangeMin;
    private Long participantsRangeMax;
    private Long questionsRangeMin;
    private Long questionsRangeMax;
    private LocalDate dateRangeMin;
    private LocalDate dateRangeMax;

    // Constructeur par défaut
    public ChallengeFilters() {}

    // Constructeur paramétré
    public ChallengeFilters(List<Long> gameModeIds, List<UserPerformance> userPerformances, GameAttributeFilter attributeFilter,
                            Long participantsRangeMin, Long participantsRangeMax,
                            Long questionsRangeMin, Long questionsRangeMax,
                            LocalDate dateRangeMin, LocalDate dateRangeMax) {
        this.gameModeIds = gameModeIds;
        this.userPerformances = userPerformances;
        this.attributeFilter = attributeFilter;
        this.participantsRangeMin = participantsRangeMin;
        this.participantsRangeMax = participantsRangeMax;
        this.questionsRangeMin = questionsRangeMin;
        this.questionsRangeMax = questionsRangeMax;
        this.dateRangeMin = dateRangeMin;
        this.dateRangeMax = dateRangeMax;
    }

    // Getters & Setters
    public List<Long> getGameModeIds() {
        return gameModeIds;
    }
    public void setGameModeIds(List<Long> gameModeIds) {
        this.gameModeIds = gameModeIds;
    }
    public List<UserPerformance> getUserPerformances() {
        return userPerformances;
    }
    public void setUserPerformances(List<UserPerformance> userPerformances) {
        this.userPerformances = userPerformances;
    }
    public GameAttributeFilter getAttributeFilter() {
        return attributeFilter;
    }
    public void setAttributeFilter(GameAttributeFilter attributeFilter) {
        this.attributeFilter = attributeFilter;
    }
    public Long getParticipantsRangeMin() {
        return participantsRangeMin;
    }
    public void setParticipantsRangeMin(Long participantsRangeMin) {
        this.participantsRangeMin = participantsRangeMin;
    }
    public Long getParticipantsRangeMax() {
        return participantsRangeMax;
    }
    public void setParticipantsRangeMax(Long participantsRangeMax) {
        this.participantsRangeMax = participantsRangeMax;
    }
    public Long getQuestionsRangeMin() {
        return questionsRangeMin;
    }
    public void setQuestionsRangeMin(Long questionsRangeMin) {
        this.questionsRangeMin = questionsRangeMin;
    }
    public Long getQuestionsRangeMax() {
        return questionsRangeMax;
    }
    public void setQuestionsRangeMax(Long questionsRangeMax) {
        this.questionsRangeMax = questionsRangeMax;
    }
    public LocalDate getDateRangeMin() {
        return dateRangeMin;
    }
    public void setDateRangeMin(LocalDate dateRangeMin) {
        this.dateRangeMin = dateRangeMin;
    }
    public LocalDate getDateRangeMax() {
        return dateRangeMax;
    }
    public void setDateRangeMax(LocalDate dateRangeMax) {
        this.dateRangeMax = dateRangeMax;
    }

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChallengeFilters)) return false;
        ChallengeFilters that = (ChallengeFilters) o;
        return Objects.equals(gameModeIds, that.gameModeIds) &&
               Objects.equals(userPerformances, that.userPerformances) &&
               Objects.equals(attributeFilter, that.attributeFilter) &&
               Objects.equals(participantsRangeMin, that.participantsRangeMin) &&
               Objects.equals(participantsRangeMax, that.participantsRangeMax) &&
               Objects.equals(questionsRangeMin, that.questionsRangeMin) &&
               Objects.equals(questionsRangeMax, that.questionsRangeMax) &&
               Objects.equals(dateRangeMin, that.dateRangeMin) &&
               Objects.equals(dateRangeMax, that.dateRangeMax);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameModeIds, userPerformances, attributeFilter, participantsRangeMin, participantsRangeMax, questionsRangeMin, questionsRangeMax, dateRangeMin, dateRangeMax);
    }

    @Override
    public String toString() {
        return "ChallengeFilters{" +
               "gameModeIds=" + gameModeIds +
               ", userPerformances=" + userPerformances +
               ", attributeFilter=" + attributeFilter +
               ", participantsRangeMin=" + participantsRangeMin +
               ", participantsRangeMax=" + participantsRangeMax +
               ", questionsRangeMin=" + questionsRangeMin +
               ", questionsRangeMax=" + questionsRangeMax +
               ", dateRangeMin=" + dateRangeMin +
               ", dateRangeMax=" + dateRangeMax +
               '}';
    }
}
