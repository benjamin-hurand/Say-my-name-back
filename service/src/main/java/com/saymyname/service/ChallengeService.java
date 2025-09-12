package com.saymyname.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.exception.ChallengeAlreadyExistsException;
import com.saymyname.core.model.challenge.Challenge;
import com.saymyname.core.model.challenge.ChallengeMenu;
import com.saymyname.core.model.challenge.ChallengeSeason;
import com.saymyname.core.model.challenge.ChallengeVersion;
import com.saymyname.persistence.dao.ChallengeDao;
import com.saymyname.persistence.projection.ChallengeCardProjection;

@Service
public class ChallengeService {

    private final ChallengeDao challengeDao;
    private final ChallengeSeasonService challengeSeasonService;
    private final ChallengeVersionService challengeVersionService;
    private final PersonAttributeService personAttributeService;
    private static final Logger logger = LoggerFactory.getLogger(ChallengeService.class);

    public ChallengeService(ChallengeDao challengeDao, ChallengeSeasonService challengeSeasonService,
            ChallengeVersionService challengeVersionService,
            PersonAttributeService personAttributeService) {
        this.challengeDao = challengeDao;
        this.challengeSeasonService = challengeSeasonService;
        this.challengeVersionService = challengeVersionService;
        this.personAttributeService = personAttributeService;
    }

    // Récupère la liste des challenges existants
    public List<ChallengeCardProjection> getChallengesList(ChallengeMenu challengeMenu) {
        return challengeDao.getChallengeCards(challengeMenu);
    }

    /**
     * Crée un nouveau challenge complet (challenge, première version et questions)
     * en utilisant la saison suivante (n+1). Si la saison suivante n'existe pas, on
     * la crée.
     */
    @Transactional
    public ChallengeVersion createNewChallenge(Challenge challenge) {
        // 1. Vérifier l'existence du challenge
        if (challengeExists(challenge)) {
            throw new ChallengeAlreadyExistsException("Un challenge avec ce mode et ce filtre existe déjà.");
        }

        // 2) Récupérer la saison suivante
        ChallengeSeason nextSeason = challengeSeasonService.getNextSeasonOrThrow();

        // 3. Vérifier qu'il existe suffisamment de questions (>=10) pour le challenge
        // dans la saison suivante
        String filterMinValue = challenge.getFilterAttribute().getMinValue();
        String filterMaxValue = challenge.getFilterAttribute().getMaxValue();
        logger.info("Filtre min: {}, Filtre max: {}", filterMinValue, filterMaxValue);
        long questionCount = personAttributeService.countPersonsMatchingFilter(
                filterMinValue,
                filterMaxValue,
                nextSeason.getStartDate(),
                challenge.getFilterAttribute().getAttribute().getId());
        if (questionCount < 10) {
            throw new IllegalArgumentException(
                    "Nombre de questions insuffisant pour ce challenge. Minimum requis : 10.");
        }

        // 4. Créer le challenge complet (challenge, première version et questions) via
        // le DAO

        // 4.1. Créer le challenge
        Challenge savedChallenge = challengeDao.saveChallenge(challenge);
        // 4.2.1 Créer la première version: model
        LocalDateTime versionStart = nextSeason.getStartDate();
        ChallengeVersion initialVersion = new ChallengeVersion.Builder()
                .withVersionNumber(1)
                .withStartDate(versionStart)
                .withEndDate(null)
                .withFirstSeason(nextSeason)
                .withChallenge(savedChallenge)
                .withQuestionCount((int) questionCount)
                .build();
        // 4.2.2 Créer la version et les questions associées
        ChallengeVersion savedChallengeVersionAndQuestions = challengeVersionService
                .createChallengeVersionAndQuestions(initialVersion, filterMaxValue, nextSeason.getStartDate());
        return savedChallengeVersionAndQuestions;
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
                max);
    }

}
