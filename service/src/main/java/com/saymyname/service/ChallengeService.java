package com.saymyname.service;

import com.saymyname.core.exception.ChallengeAlreadyExistsException;
import com.saymyname.core.model.challenge.Challenge;
import com.saymyname.core.model.challenge.ChallengeMenu;
import com.saymyname.core.model.challenge.ChallengeSeason;
import com.saymyname.core.model.challenge.ChallengeVersion;
import com.saymyname.persistence.dao.ChallengeDao;
import com.saymyname.persistence.projection.ChallengeCardProjection;
import com.saymyname.persistence.repository.PersonAttributeRepository;
import com.saymyname.service.ChallengeSeasonService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ChallengeService {

    private final ChallengeDao challengeDao;
    private final ChallengeSeasonService challengeSeasonService;
    private final PersonAttributeRepository personAttributeRepository;
    private static final Logger logger = LoggerFactory.getLogger(ChallengeService.class);


    public ChallengeService(ChallengeDao challengeDao, ChallengeSeasonService challengeSeasonService, PersonAttributeRepository personAttributeRepository) {
        this.challengeDao = challengeDao;
        this.challengeSeasonService = challengeSeasonService;
        this.personAttributeRepository = personAttributeRepository;
    }

    // Récupère la liste des challenges existants
    public List<ChallengeCardProjection> getChallengesList(ChallengeMenu challengeMenu) {
        return challengeDao.getChallengeCards(challengeMenu);
    }

    /**
     * Crée un nouveau challenge complet (challenge, première version et questions)
     * en utilisant la saison suivante (n+1). Si la saison suivante n'existe pas, on la crée.
     */
    @Transactional
    public ChallengeVersion createNewChallenge(Challenge challenge) {
        // 1. Vérifier l'existence du challenge
        if (challengeExists(challenge)) {
            throw new ChallengeAlreadyExistsException("Un challenge avec ce mode et ce filtre existe déjà.");
        }
        
        // 2. Récupérer la saison suivante via le service
        Optional<ChallengeSeason> nextSeasonOpt = challengeSeasonService.getNextSeason();
        if (nextSeasonOpt.isEmpty()) {
            // Si la saison suivante n'existe pas, la créer puis la récupérer
            challengeSeasonService.createNextSeasonIfMissing();
            nextSeasonOpt = challengeSeasonService.getNextSeason();
        }
        ChallengeSeason nextSeason = nextSeasonOpt.get();

        // 3. Vérifier qu'il existe suffisamment de questions (>=10) pour le challenge dans la saison suivante
        long questionCount = personAttributeRepository.countPersonsMatchingFilter(
                                challenge.getFilterAttribute().getMinValue(),
                                challenge.getFilterAttribute().getMaxValue(),
                                nextSeason.getStartDate(),
                                challenge.getFilterAttribute().getAttribute().getId()
                            );
        if (questionCount < 10) {
            throw new IllegalArgumentException("Nombre de questions insuffisant pour ce challenge. Minimum requis : 10.");
        }

        // 4. Créer le challenge complet (challenge, première version et questions) via le DAO
        return challengeDao.createChallengeWithVersionAndQuestions(challenge, nextSeason, questionCount);
    }

    public Boolean challengeExists(Challenge challenge) {
        String min = challenge.getFilterAttribute().getMinValue() != null
                     ? challenge.getFilterAttribute().getMinValue().trim()
                     : "";
        String max = challenge.getFilterAttribute().getMaxValue() != null
                     ? challenge.getFilterAttribute().getMaxValue().trim()
                     : "";
        return challengeDao.challengeExists(
                challenge.getGameMode().getId(),
                challenge.getFilterAttribute().getAttribute().getId(),
                min,
                max
        );
    }
    
}
