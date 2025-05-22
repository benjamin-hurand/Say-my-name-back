package com.saymyname.persistence.dao;

import java.time.LocalDateTime;
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

    public ChallengeDao(ChallengeRepository challengeRepository,
            ChallengeEntityMapper challengeEntityMapper) {
        this.challengeRepository = challengeRepository;
        this.challengeEntityMapper = challengeEntityMapper;
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
