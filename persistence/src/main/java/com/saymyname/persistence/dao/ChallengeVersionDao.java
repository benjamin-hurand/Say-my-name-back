package com.saymyname.persistence.dao;

import org.springframework.stereotype.Repository;

import com.saymyname.core.model.challenge.Challenge;
import com.saymyname.core.model.challenge.ChallengeVersion;
import com.saymyname.persistence.entity.ChallengeVersionEntity;
import com.saymyname.persistence.mapper.ChallengeVersionEntityMapper;
import com.saymyname.persistence.repository.ChallengeVersionRepository;

@Repository
public class ChallengeVersionDao {

    private ChallengeVersionRepository challengeVersionRepository;
    private ChallengeVersionEntityMapper challengeVersionEntityMapper;

    public ChallengeVersionDao(ChallengeVersionRepository challengeVersionRepository,
            ChallengeVersionEntityMapper challengeVersionEntityMapper) {
        this.challengeVersionEntityMapper = challengeVersionEntityMapper;
        this.challengeVersionRepository = challengeVersionRepository;
    }

    public ChallengeVersion saveChallengeVersion(ChallengeVersion challengeVersion) {
        ChallengeVersionEntity versionEntity = challengeVersionEntityMapper.toEntity(challengeVersion);
        ChallengeVersionEntity savedVersionEntity = challengeVersionRepository.save(versionEntity);
        return challengeVersionEntityMapper.toModelWithoutQuestions(savedVersionEntity);
    }
}
