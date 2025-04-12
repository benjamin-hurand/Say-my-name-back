package com.saymyname.persistence.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.challenge.Challenge;
import com.saymyname.core.model.challenge.ChallengeSeason;
import com.saymyname.core.model.challenge.ChallengeVersion;
import com.saymyname.core.model.challenge.ChallengeMenu;
import com.saymyname.persistence.config.DataSourceConfig;
import com.saymyname.persistence.entity.ChallengeEntity;
import com.saymyname.persistence.entity.ChallengeVersionEntity;
import com.saymyname.persistence.mapper.ChallengeEntityMapper;
import com.saymyname.persistence.mapper.ChallengeSeasonEntityMapper;
import com.saymyname.persistence.mapper.ChallengeVersionEntityMapper;
import com.saymyname.persistence.projection.ChallengeCardProjection;
import com.saymyname.persistence.repository.ChallengeQuestionRepository;
import com.saymyname.persistence.repository.ChallengeRepository;
import com.saymyname.persistence.repository.ChallengeVersionRepository;

@Repository
public class ChallengeDao {

    private final ChallengeRepository challengeRepository;
    private final ChallengeEntityMapper challengeEntityMapper;
    private final ChallengeVersionRepository challengeVersionRepository;
    private final ChallengeSeasonEntityMapper challengeSeasonEntityMapper;
    private final ChallengeQuestionRepository challengeQuestionRepository;
    private final ChallengeVersionEntityMapper challengeVersionEntityMapper;
    private static final Logger logger = LoggerFactory.getLogger(ChallengeDao.class);


    public ChallengeDao(ChallengeRepository challengeRepository,
                        ChallengeEntityMapper challengeEntityMapper,
                        ChallengeVersionRepository challengeVersionRepository,
                        ChallengeSeasonEntityMapper challengeSeasonEntityMapper,
                        ChallengeQuestionRepository challengeQuestionRepository,
                        ChallengeVersionEntityMapper challengeVersionEntityMapper) {
        this.challengeRepository = challengeRepository;
        this.challengeEntityMapper = challengeEntityMapper;
        this.challengeVersionRepository = challengeVersionRepository;
        this.challengeSeasonEntityMapper = challengeSeasonEntityMapper;
        this.challengeQuestionRepository = challengeQuestionRepository;
        this.challengeVersionEntityMapper = challengeVersionEntityMapper;
    }

    @Transactional(readOnly = true)
    public List<ChallengeCardProjection> getChallengeCards(ChallengeMenu challengeMenu) {
        return challengeRepository.findChallengeCards(challengeMenu);
    }

    public boolean challengeExists(Long modeId, Long filterId, String minFilterValue, String maxFilterValue) {
        return challengeRepository
                .findByGameMode_IdAndFilterAttribute_IdAndMinFilterValueAndMaxFilterValue(modeId, filterId, minFilterValue, maxFilterValue)
                .isPresent();
    }

    @Transactional
public ChallengeVersion createChallengeWithVersionAndQuestions(
        Challenge challenge,
        ChallengeSeason nextSeason,
        long questionCount
) {
    // 1. Sauvegarder le challenge
    ChallengeEntity challengeEntity = challengeEntityMapper.toEntity(challenge);
    if (challengeEntity.getCreationDate() == null) {
            challengeEntity.setCreationDate(LocalDateTime.now());
        }
        ChallengeEntity savedChallenge = challengeRepository.save(challengeEntity);

        // 2. Créer la première version
        LocalDateTime versionStart = nextSeason.getStartDate();
        ChallengeVersionEntity initialVersion = new ChallengeVersionEntity.Builder()
                .withVersionNumber(1)
                .withStartDate(versionStart)
                .withEndDate(null)
                .withFirstSeason(challengeSeasonEntityMapper.toEntity(nextSeason))
                .withChallenge(savedChallenge)
                .withQuestionCount((int) questionCount)
                .build();
        ChallengeVersionEntity savedVersion = challengeVersionRepository.save(initialVersion);

        // 3. Insérer les questions
        String filterMax = challenge.getFilterAttribute().getMaxValue();
        String nextFilterMax = nextValue(filterMax);
        challengeQuestionRepository.insertChallengeQuestions(
                savedChallenge.getId(),
                nextFilterMax,
                nextSeason.getStartDate(),
                savedVersion.getId()
        );

        // 4. Convertir l'entité ChallengeVersionEntity vers votre modèle métier ChallengeVersion
        ChallengeVersion modelVersion = challengeVersionEntityMapper.toModel(savedVersion);

        // 5. Retourner la version nouvellement créée
        return modelVersion;
    }

    private String nextValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.length() == 1) {
            char c = value.charAt(0);
            // Si c'est 'Z' ou 'z', retourner une borne supérieure large
            return (c == 'Z' || c == 'z') ? (value.equals("Z") ? "Z\uffff" : "z\uffff") : String.valueOf((char) (c + 1));
        }
        // Pour une chaîne plus longue, on peut ajouter un caractère maximum à la fin
        return value + "\uffff";
    }
}
