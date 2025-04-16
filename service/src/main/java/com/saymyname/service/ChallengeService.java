package com.saymyname.service;

import com.saymyname.core.exception.ChallengeAlreadyExistsException;
import com.saymyname.core.model.challenge.Challenge;
import com.saymyname.core.model.challenge.ChallengeMenu;
import com.saymyname.core.model.challenge.ChallengeSeason;
import com.saymyname.core.model.challenge.ChallengeVersion;
import com.saymyname.persistence.dao.ChallengeDao;
import com.saymyname.persistence.dao.PersonAttributeDao;
import com.saymyname.persistence.entity.ChallengeVersionEntity;
import com.saymyname.persistence.projection.ChallengeCardProjection;
import com.saymyname.persistence.repository.PersonAttributeRepository;
import com.saymyname.service.ChallengeSeasonService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChallengeService {

    private final ChallengeDao challengeDao;
    private final ChallengeSeasonService challengeSeasonService;
    private final PersonAttributeDao personAttributeDao;
    private final ChallengeVersionService challengeVersionService;
    private static final Logger logger = LoggerFactory.getLogger(ChallengeService.class);

    public ChallengeService(ChallengeDao challengeDao, ChallengeSeasonService challengeSeasonService,
            PersonAttributeDao personAttributeDao, ChallengeVersionService challengeVersionService) {
        this.challengeDao = challengeDao;
        this.challengeSeasonService = challengeSeasonService;
        this.personAttributeDao = personAttributeDao;
        this.challengeVersionService = challengeVersionService;
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

        // 2. Récupérer la saison suivante via le service
        Optional<ChallengeSeason> nextSeasonOpt = challengeSeasonService.getNextSeason();
        if (nextSeasonOpt.isEmpty()) {
            // Si la saison suivante n'existe pas, la créer puis la récupérer
            challengeSeasonService.createNextSeasonIfMissing();
            nextSeasonOpt = challengeSeasonService.getNextSeason();
        }
        ChallengeSeason nextSeason = nextSeasonOpt.get();

        // 3. Vérifier qu'il existe suffisamment de questions (>=10) pour le challenge
        // dans la saison suivante
        long questionCount = personAttributeDao.countPersonsMatchingFilter(
                challenge.getFilterAttribute().getMinValue(),
                challenge.getFilterAttribute().getMaxValue(),
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
        // 4.2.2 Déduire la valeur du filtre max pour faciliter la requête SQL.
        String nextFilterMax = nextValue(challenge.getFilterAttribute().getMaxValue());
        // 4.2.3 Créer la version et les questions associées
        ChallengeVersion savedChallengeVersionAndQuestions = challengeVersionService
                .createChallengeVersionAndQuestions(initialVersion, nextFilterMax, nextSeason.getStartDate());
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

    private String nextValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.length() == 1) {
            char c = value.charAt(0);
            // Si c'est 'Z' ou 'z', retourner une borne supérieure large
            return (c == 'Z' || c == 'z') ? (value.equals("Z") ? "Z\uffff" : "z\uffff")
                    : String.valueOf((char) (c + 1));
        }
        // Pour une chaîne plus longue, on peut ajouter un caractère maximum à la fin
        return value + "\uffff";
    }

}
