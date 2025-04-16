package com.saymyname.persistence.dao;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.core.model.challenge.Challenge;
import com.saymyname.core.model.challenge.ChallengeQuestion;
import com.saymyname.persistence.entity.ChallengeQuestionEntity;
import com.saymyname.persistence.mapper.ChallengeQuestionEntityMapper;
import com.saymyname.persistence.repository.ChallengeQuestionRepository;

@Repository
public class ChallengeQuestionDao {
    private ChallengeQuestionRepository challengeQuestionRepository;
    private final ChallengeQuestionEntityMapper challengeQuestionEntityMapper;

    public ChallengeQuestionDao(ChallengeQuestionRepository challengeQuestionRepository,
            ChallengeQuestionEntityMapper challengeQuestionEntityMapper) {
        this.challengeQuestionEntityMapper = challengeQuestionEntityMapper;
        this.challengeQuestionRepository = challengeQuestionRepository;
    }

    public List<ChallengeQuestion> findByVersionId(Long versionId) {
        List<ChallengeQuestionEntity> entities = challengeQuestionRepository.findByVersionId(versionId);
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(challengeQuestionEntityMapper::toModel)
                .toList();
    }

    public void saveQuestionsOfVersion(Long challengeId, String nextFilterMax, LocalDateTime nextSeasonStartDate,
            Long versionId) {
        challengeQuestionRepository.insertChallengeQuestions(
                challengeId,
                nextFilterMax,
                nextSeasonStartDate,
                versionId);
    }

}
