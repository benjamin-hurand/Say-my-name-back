package com.saymyname.webapp.mapper.challenge;

import com.saymyname.persistence.projection.ChallengeCardProjection;
import com.saymyname.webapp.dto.challenge.ChallengeAttemptDto;
import com.saymyname.webapp.dto.challenge.ChallengeCardDto;
import com.saymyname.webapp.dto.challenge.ChallengeCreatorDto;
import com.saymyname.webapp.dto.challenge.ChallengeFilterDto;
import com.saymyname.webapp.dto.challenge.ChallengeGameModeDto;
import com.saymyname.webapp.dto.challenge.ChallengeInfoDto;
import com.saymyname.webapp.dto.challenge.ChallengeVersionDto;

import org.springframework.stereotype.Component;

@Component
public class ChallengeCardDtoMapper {

        public ChallengeCardDto toDto(ChallengeCardProjection projection) {
                // Création du DTO pour le créateur
                ChallengeCreatorDto creator = new ChallengeCreatorDto(
                                projection.getCreatorId(),
                                projection.getCreatorUsername());

                // Création du DTO pour le filtre (attribut)
                ChallengeFilterDto filter = new ChallengeFilterDto(
                                projection.getFilterAttributeId(),
                                projection.getAttributeName(),
                                projection.getFilterType(),
                                projection.getMinFilterValue(),
                                projection.getMaxFilterValue());

                // Création du DTO pour le mode de jeu (uniquement title et description)
                ChallengeGameModeDto gameMode = new ChallengeGameModeDto(
                                projection.getGameModeId(),
                                projection.getGameModeTitle(),
                                projection.getGameModeDescription());

                // Création du DTO pour les informations du challenge
                ChallengeInfoDto challengeInfo = new ChallengeInfoDto(
                                projection.getChallengeId(),
                                projection.getDescription(),
                                projection.getCreationDate(),
                                filter,
                                gameMode,
                                creator);

                // Création du DTO pour la version du challenge
                ChallengeVersionDto version = new ChallengeVersionDto(
                                projection.getChallengeVersionId(),
                                projection.getVersionNumber(),
                                projection.getVersionStartDate(),
                                projection.getVersionEndDate(),
                                projection.getQuestionCount());

                // Création du DTO pour la tentative (données calculées)
                ChallengeAttemptDto attempt = new ChallengeAttemptDto(
                                projection.getNbParticipants(),
                                projection.getBestQuestionScore(),
                                projection.getBestTimeMs(),
                                projection.getAttemptStartDate());

                // Retourne le DTO complet
                return new ChallengeCardDto(challengeInfo, version, attempt);
        }
}
