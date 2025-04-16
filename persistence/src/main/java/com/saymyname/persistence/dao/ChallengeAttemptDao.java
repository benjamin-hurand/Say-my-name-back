package com.saymyname.persistence.dao;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.saymyname.core.model.challenge.ChallengeAttempt;
import com.saymyname.persistence.entity.ChallengeAttemptEntity;
import com.saymyname.persistence.mapper.ChallengeAttemptEntityMapper;
import com.saymyname.persistence.repository.ChallengeAttemptRepository;

import jakarta.transaction.Transactional;

@Repository
public class ChallengeAttemptDao {

    private final Logger logger = LoggerFactory.getLogger(ChallengeAttemptDao.class);

    private ChallengeAttemptRepository challengeAttemptRepository;
    private ChallengeAttemptEntityMapper challengeAttemptEntityMapper;

    public ChallengeAttemptDao(ChallengeAttemptRepository challengeAttemptRepository,
            ChallengeAttemptEntityMapper challengeAttemptEntityMapper) {
        this.challengeAttemptEntityMapper = challengeAttemptEntityMapper;
        this.challengeAttemptRepository = challengeAttemptRepository;
    }

    /**
     * Crée une nouvelle tentative de challenge.
     *
     * @param challengeAttempt La tentative de challenge à créer.
     * @return La tentative de challenge créée.
     */
    @Transactional
    public ChallengeAttempt createChallengeAttempt(ChallengeAttempt challengeAttempt) {
        ChallengeAttemptEntity challengeAttemptEntity = challengeAttemptEntityMapper.toEntity(challengeAttempt);
        if (challengeAttemptEntity == null) {
            throw new IllegalArgumentException("ChallengeAttemptEntity cannot be null");
        }
        ChallengeAttemptEntity savedEntity = challengeAttemptRepository.save(challengeAttemptEntity);
        if (savedEntity == null) {
            throw new IllegalArgumentException("Failed to save ChallengeAttemptEntity");
        }

        ChallengeAttempt savedChallengeAttempt = challengeAttemptEntityMapper.toModel(savedEntity);
        if (savedChallengeAttempt == null) {
            throw new IllegalArgumentException("Saved ChallengeAttempt cannot be null");
        }
        return savedChallengeAttempt;
    }

    /**
     * Récupère une tentative de challenge par son ID.
     *
     * @param id L'ID de la tentative de challenge à récupérer.
     * @return La tentative de challenge correspondante, ou null si non trouvée.
     */
    @Transactional
    public ChallengeAttempt findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        ChallengeAttemptEntity challengeAttemptEntity = challengeAttemptRepository.findById(id).orElse(null);
        if (challengeAttemptEntity != null) {
            Hibernate.initialize(challengeAttemptEntity.getChallengeVersion());
            Hibernate.initialize(challengeAttemptEntity.getChallengeVersion().getQuestions());
        }
        return challengeAttemptEntityMapper.toModel(challengeAttemptEntity);
    }
}
