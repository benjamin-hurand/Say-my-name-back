package com.saymyname.persistence.dao;

import java.time.LocalDateTime;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.saymyname.core.exception.ChallengeAttemptException;
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

    @Transactional
    public void startAttempt(Long id) throws ChallengeAttemptException {
        ChallengeAttemptEntity entity = challengeAttemptRepository.findById(id)
                .orElseThrow(() -> new ChallengeAttemptException("Attempt not found: " + id));
        entity.setAttemptStart(LocalDateTime.now());
        challengeAttemptRepository.save(entity);
    }

    @Transactional
    public void stopAttempt(Long id) throws ChallengeAttemptException {
        ChallengeAttemptEntity entity = challengeAttemptRepository.findById(id)
                .orElseThrow(() -> new ChallengeAttemptException("Attempt not found: " + id));
        entity.setAttemptEnd(LocalDateTime.now());
        challengeAttemptRepository.save(entity);
    }

    /**
     * Met à jour le nombre de bonnes réponses d'une tentative existante.
     *
     * @param attemptId    l'ID de la tentative
     * @param totalCorrect le nouveau total de réponses correctes
     * @throws ChallengeAttemptException si la tentative n'existe pas
     */
    @Transactional
    public void updateCorrectAnswers(Long attemptId, int totalCorrect) throws ChallengeAttemptException {
        // 1. Charger l'entité existante
        ChallengeAttemptEntity entity = challengeAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ChallengeAttemptException("Tentative non trouvée pour id=" + attemptId));

        // 2. Modifier la propriété
        entity.setCorrectAnswers(totalCorrect);

        // 3. Persister (=> INSERT si id null, UPDATE sinon)
        challengeAttemptRepository.save(entity);
    }
}
