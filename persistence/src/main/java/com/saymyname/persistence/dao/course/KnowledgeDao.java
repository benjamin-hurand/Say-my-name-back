package com.saymyname.persistence.dao.course;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.persistence.entity.course.KnowledgeEntity;
import com.saymyname.persistence.mapper.course.KnowledgeEntityMapper;
import com.saymyname.persistence.repository.course.KnowledgeRepository;

@Repository
@Transactional
public class KnowledgeDao {

    private final KnowledgeRepository knowledgeRepository;
    private final KnowledgeEntityMapper knowledgeEntityMapper;

    private static final double INITIAL_EF = 2.5;
    private static final double INITIAL_DIFF = 1.0;
    private static final int BATCH_SIZE = 10;

    public KnowledgeDao(KnowledgeRepository knowledgeRepository, KnowledgeEntityMapper knowledgeEntityMapper) {
        this.knowledgeEntityMapper = knowledgeEntityMapper;
        this.knowledgeRepository = knowledgeRepository;
    }

    public int insertBatchOfTenKnowledges(Course course) {
        return knowledgeRepository.insertNextKnowledgesForCourse(
                course.getId(),
                course.getUser().getId(),
                course.getGameMode().getId(),
                course.getSortingAttribute().getId(),
                course.getSortingOrder(),
                INITIAL_EF,
                INITIAL_DIFF,
                BATCH_SIZE);
    }

    public int countByCourseAndStatus(Course course, KnowledgeStatus status) {
        return knowledgeRepository.countByUserIdAndGameModeIdAndStatusIn(
                course.getUser().getId(),
                course.getGameMode().getId(),
                status.scope());
    }

    /** Récupère une connaissance précise sur une personne précise. */
    public Knowledge findByUserGameModeAndPerson(Long userId, Long gameModeId, Long personId) {
        KnowledgeEntity entity = knowledgeRepository.findByUserIdAndGameModeIdAndPersonId(userId, gameModeId, personId);
        return knowledgeEntityMapper.toModel(entity);
    }

    /** Upsert de la connaissance après calcul SM-2/PFA/FSRS. */
    public void upsertKnowledge(Knowledge k) {
        knowledgeRepository.upsertKnowledge(
                k.getUser().getId(),
                k.getGameMode().getId(),
                k.getPerson().getId(),
                k.getNextReviewDate(),
                k.getTotalRepetitionCount(),
                k.getSrsStreak(),
                k.getGlobalStreak(),
                k.getEaseFactor(),
                k.getStatus().name(),
                k.getLastReviewDate(),
                k.getSuccessCount(),
                k.getFailureCount(),
                k.getStability(),
                k.getDifficulty());
    }

    // POOLS
    // UNKNOWN
    public Knowledge findFirstNew(long userId, long gameModeId, Long lastPersonId, boolean allowRepeat) {
        KnowledgeEntity entity = knowledgeRepository.findFirstNewItem(userId, gameModeId, lastPersonId, allowRepeat);
        return knowledgeEntityMapper.toModel(entity);
    }

    // DISCOVERED
    public Knowledge findFirstDiscovered(long userId, long gameModeId, Long lastPersonId, boolean allowRepeat) {
        KnowledgeEntity entity = knowledgeRepository.findFirstNotSoNewItem(userId, gameModeId, lastPersonId,
                allowRepeat);
        return knowledgeEntityMapper.toModel(entity);
    }

    // LEARNED: recent errors
    public Knowledge findFirstRecentError(long userId, long gameModeId, Long lastPersonId, boolean allowRepeat) {
        KnowledgeEntity entity = knowledgeRepository.findFirstRecentError(userId, gameModeId, lastPersonId,
                allowRepeat);
        return knowledgeEntityMapper.toModel(entity);
    }

    // LEARNED: srs due
    public Knowledge findFirstSRS(long userId, long gameModeId, Long lastPersonId, boolean allowRepeat) {
        KnowledgeEntity entity = knowledgeRepository
                .findFirstSrsDue(userId,
                        gameModeId, lastPersonId, allowRepeat);
        return knowledgeEntityMapper.toModel(entity);
    }

    // [BONUS REVISION] MASTERED and LEARNED: future srs due
    public Knowledge findRevision(long userId, long gameModeId, Long lastPersonId, boolean allowRepeat) {
        KnowledgeEntity entity = knowledgeRepository.findRevision(userId, gameModeId, lastPersonId, allowRepeat);
        return knowledgeEntityMapper.toModel(entity);
    }

    public void update(Knowledge knowledge) {
        knowledgeRepository.save(knowledgeEntityMapper.toEntity(knowledge));
    }

    public List<Knowledge> findAllByCourse(Course course) {
        return knowledgeRepository
                .findByGameModeIdAndUserIdAndStatusNot(course.getGameMode().getId(), course.getUser().getId(),
                        KnowledgeStatus.MASTERED)
                .stream()
                .map(knowledgeEntityMapper::toModel).toList();
    }
}
