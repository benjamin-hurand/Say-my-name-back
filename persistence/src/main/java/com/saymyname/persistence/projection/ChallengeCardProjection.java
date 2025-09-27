package com.saymyname.persistence.projection;

import java.time.LocalDateTime;

import com.saymyname.core.model.people.AttributeType;

public interface ChallengeCardProjection {
    // Champs du challenge
    Long getChallengeId();

    String getDescription();

    LocalDateTime getCreationDate();

    String getMinFilterValue();

    String getMaxFilterValue();

    // Champs de la version
    Long getChallengeVersionId();

    Integer getVersionNumber();

    LocalDateTime getVersionStartDate();

    LocalDateTime getVersionEndDate();

    Integer getQuestionCount();

    // Champs calculés existants
    Long getNbParticipants();

    Integer getBestQuestionScore();

    Long getBestTimeMs();

    // Champs du filtre (attribut)
    Long getFilterAttributeId();

    String getAttributeName();

    AttributeType getFilterType();

    // Champs du game mode
    Long getGameModeId();

    String getGameModeTitle();

    String getGameModeDescription();

    // Champs du creator (ajoutés)
    Long getCreatorId();

    String getCreatorUsername();

    // Champs de l'attempt
    LocalDateTime getAttemptStartDate();
}
