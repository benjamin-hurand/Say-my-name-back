package com.saymyname.persistence.dao;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.challenge.Challenge;
import com.saymyname.core.model.challenge.ChallengeMenu;
import com.saymyname.persistence.entity.ChallengeEntity;
import com.saymyname.persistence.mapper.ChallengeEntityMapper;
import com.saymyname.persistence.projection.ChallengeCardProjection;
import com.saymyname.persistence.repository.ChallengeRepository;

@Repository
public class ChallengeDao {

    private final ChallengeRepository challengeRepository;
    private final ChallengeEntityMapper challengeEntityMapper;

    public ChallengeDao(ChallengeRepository challengeRepository, ChallengeEntityMapper challengeEntityMapper) {
        this.challengeRepository = challengeRepository;
        this.challengeEntityMapper = challengeEntityMapper;
    }

    /**
     * Récupère une liste de ChallengeCardProjection à partir des critères fournis dans le ChallengeMenu.
     * La méthode utilise une projection pour ne récupérer que les données nécessaires pour l'affichage côté front.
     *
     * @param challengeMenu les critères de filtrage encapsulés dans un ChallengeMenu
     * @return la liste des ChallengeCardProjection correspondants
     */
    @Transactional(readOnly = true)
    public List<ChallengeCardProjection> getChallengeCards(ChallengeMenu challengeMenu) {
        return challengeRepository.findChallengeCards(challengeMenu);
    }

    /**
     * Vérifie si un challenge avec le même mode et filtre existe déjà.
     */
    public boolean challengeExists(Long modeId, Long filterId, String minFilterValue, String maxFilterValue) {
        return challengeRepository
                .findByGameMode_IdAndFilterAttribute_IdAndMinFilterValueAndMaxFilterValue(modeId, filterId, minFilterValue, maxFilterValue)
                .isPresent();
    }

    /**
     * Sauvegarde un challenge dans la base de données.
     *
     * @param challenge le modèle de domaine Challenge à sauvegarder
     * @return le modèle Challenge sauvegardé
     */
    @Transactional
    public Challenge saveChallenge(Challenge challenge) {
        // Mapper le modèle en entité
        ChallengeEntity entity = challengeEntityMapper.toEntity(challenge);
        // Sauvegarde dans la base via le repository
        ChallengeEntity savedEntity = challengeRepository.save(entity);
        // Retourner le modèle converti depuis l'entité sauvegardée
        return challengeEntityMapper.toModel(savedEntity);
    }
}
