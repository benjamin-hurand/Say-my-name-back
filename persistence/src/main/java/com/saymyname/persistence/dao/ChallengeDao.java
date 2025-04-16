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
                .findByGameMode_IdAndFilterAttribute_IdAndMinFilterValueAndMaxFilterValue(modeId, filterId,
                        minFilterValue, maxFilterValue)
                .isPresent();
    }

    public Challenge saveChallenge(Challenge challenge) {
        ChallengeEntity challengeEntity = challengeEntityMapper.toEntity(challenge);
        if (challengeEntity.getCreationDate() == null) {
            challengeEntity.setCreationDate(LocalDateTime.now());
        }
        ChallengeEntity savedChallenge = challengeRepository.save(challengeEntity);
        return challengeEntityMapper.toModel(savedChallenge);
    }

}
