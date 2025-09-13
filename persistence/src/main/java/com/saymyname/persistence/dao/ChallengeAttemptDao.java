package com.saymyname.persistence.dao;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.saymyname.core.exception.ChallengeAttemptAlreadyEndedException;
import com.saymyname.core.exception.ChallengeAttemptAlreadyStartedException;
import com.saymyname.core.exception.ChallengeAttemptException;
import com.saymyname.core.exception.ChallengeAttemptNotFoundException;
import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.challenge.ChallengeAttempt;
import com.saymyname.core.model.enums.AttemptStatus;
import com.saymyname.persistence.entity.ChallengeAttemptEntity;
import com.saymyname.persistence.entity.ChallengeQuestionEntity;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.mapper.ChallengeAttemptEntityMapper;
import com.saymyname.persistence.mapper.UserEntityMapper;
import com.saymyname.persistence.repository.ChallengeAttemptRepository;

import jakarta.transaction.Transactional;

@Repository
public class ChallengeAttemptDao {

    private final Logger logger = LoggerFactory.getLogger(ChallengeAttemptDao.class);

    private ChallengeAttemptRepository challengeAttemptRepository;
    private ChallengeAttemptEntityMapper challengeAttemptEntityMapper;
    private UserEntityMapper userEntityMapper;

    public ChallengeAttemptDao(ChallengeAttemptRepository challengeAttemptRepository,
            ChallengeAttemptEntityMapper challengeAttemptEntityMapper,
            UserEntityMapper userEntityMapper) {
        this.challengeAttemptEntityMapper = challengeAttemptEntityMapper;
        this.challengeAttemptRepository = challengeAttemptRepository;
        this.userEntityMapper = userEntityMapper;
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
            Hibernate.initialize(challengeAttemptEntity.getChallengeVersion().getChallenge());
            challengeAttemptEntity.getChallengeVersion().getQuestions()
                    .forEach(q -> {
                        Hibernate.initialize(q.getPerson());
                        Hibernate.initialize(q.getPerson().getAttributes());
                    });
        }
        return challengeAttemptEntityMapper.toModel(challengeAttemptEntity);
    }

    public ChallengeAttempt findByIdWithAll(Long id) {
        ChallengeAttemptEntity entity = challengeAttemptRepository
                .findByIdWithAll(id)
                .orElseThrow(() -> new IllegalArgumentException("Tentative introuvable pour id=" + id));
        entity.getChallengeVersion().getQuestions()
                .stream()
                .map(ChallengeQuestionEntity::getPerson)
                .forEach(p -> Hibernate.initialize(p.getAttributes()));
        return challengeAttemptEntityMapper.toModel(entity);
    }

    @Transactional
    public void startAttempt(Long id) {
        ChallengeAttemptEntity entity = challengeAttemptRepository.findById(id)
                .orElseThrow(() -> new ChallengeAttemptNotFoundException(id));

        if (entity.getAttemptStart() != null) {
            throw new ChallengeAttemptAlreadyStartedException(id);
        }

        entity.setAttemptStart(LocalDateTime.now());
        challengeAttemptRepository.save(entity);
    }

    @Transactional
    public void stopAttempt(Long id) {
        ChallengeAttemptEntity entity = challengeAttemptRepository.findById(id)
                .orElseThrow(() -> new ChallengeAttemptNotFoundException(id));

        if (entity.getAttemptEnd() != null) {
            throw new ChallengeAttemptAlreadyEndedException(id);
        }

        entity.setAttemptEnd(LocalDateTime.now());
        challengeAttemptRepository.save(entity);
    }

    // Une methode avec commentaire pour supprimer les tentatives de challenge d'un
    // utilisateur si elles n'ont pas de startDate ou de endDate
    @Transactional
    public void deleteAttemptsWithoutStartOrEndDate(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        UserEntity userEntity = userEntityMapper.toEntity(user);
        if (userEntity == null) {
            throw new IllegalArgumentException("UserEntity cannot be null");
        }
        challengeAttemptRepository.deleteIncompleteAttemptsByUserId(userEntity.getId());
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

    public boolean verifyUserCanAttempt(Long userId, Long attemptId) {
        var bool = challengeAttemptRepository.existsByIdAndUserIdAndAttemptStartIsNullAndAttemptEndIsNull(attemptId,
                userId);
        logger.info("Tentative existante pour userId={} et attemptId={}: {}", userId, attemptId, bool);
        return bool;
    }

    public void markAbandoned(Long id) {
        challengeAttemptRepository.markAbandoned(id);
    }

    public List<ChallengeAttemptEntity> findStaleAttempts(Duration maxAge) {
        LocalDateTime cutoff = LocalDateTime.now().minus(maxAge);
        return challengeAttemptRepository.findByStatusAndAttemptStartBefore(AttemptStatus.IN_PROGRESS, cutoff);
    }

    public void saveAll(List<ChallengeAttemptEntity> attempts) {
        challengeAttemptRepository.saveAll(attempts);
    }
}
