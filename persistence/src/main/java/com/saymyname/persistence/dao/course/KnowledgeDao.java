package com.saymyname.persistence.dao.course;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.PopulationScope;
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
    private static final double INITIAL_STABILITY = 1.0;
    private static final int BATCH_SIZE = 10;

    public KnowledgeDao(KnowledgeRepository knowledgeRepository, KnowledgeEntityMapper knowledgeEntityMapper) {
        this.knowledgeEntityMapper = knowledgeEntityMapper;
        this.knowledgeRepository = knowledgeRepository;
    }

    /** Insère un batch d’UNKNOWN selon le scope du course (FOLLOWED / ALL) */
    public int insertBatchOfTenKnowledges(Course course) {
        PopulationScope scope = course.getPopulationScope() != null ? course.getPopulationScope()
                : PopulationScope.FOLLOWED;
        return switch (scope) {
            case FOLLOWED -> knowledgeRepository.insertNextKnowledgesForCourseFollowed(
                    course.getUser().getId(),
                    course.getGameMode().getId(),
                    INITIAL_EF,
                    INITIAL_DIFF,
                    INITIAL_STABILITY,
                    BATCH_SIZE);
            case ALL -> knowledgeRepository.insertNextKnowledgesForCourseAll(
                    course.getUser().getId(),
                    course.getGameMode().getId(),
                    INITIAL_EF,
                    INITIAL_DIFF,
                    INITIAL_STABILITY,
                    BATCH_SIZE);
        };
    }

    public int countByCourseAndStatus(Course course, KnowledgeStatus status) {
        return knowledgeRepository.countByUserIdAndGameModeIdAndStatusIn(
                course.getUser().getId(),
                course.getGameMode().getId(),
                status.scope());
    }

    /** Récupère une connaissance précise sur une personne précise. */
    public Optional<Knowledge> findByUserGameModeAndPerson(Long userId, Long gameModeId, Long personId) {
        Optional<KnowledgeEntity> entity = knowledgeRepository.findByUserIdAndGameModeIdAndPersonId(userId, gameModeId,
                personId);
        return entity.map(knowledgeEntityMapper::toModel);
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

    // --------- POOLS : signatures basées sur Course pour router via le scope -----

    /** UNKNOWN */
    public Knowledge findFirstNew(Course course, Long lastPersonId, boolean allowRepeat) {
        var scope = course.getPopulationScope() != null ? course.getPopulationScope() : PopulationScope.FOLLOWED;
        KnowledgeEntity entity = switch (scope) {
            case FOLLOWED -> knowledgeRepository.findFirstNewItemFollowed(
                    course.getUser().getId(), course.getGameMode().getId(), lastPersonId, allowRepeat);
            case ALL -> knowledgeRepository.findFirstNewItemAll(
                    course.getUser().getId(), course.getGameMode().getId(), lastPersonId, allowRepeat);
        };
        return knowledgeEntityMapper.toModel(entity);
    }

    /** DISCOVERED */
    public Knowledge findFirstDiscovered(Course course, Long lastPersonId, boolean allowRepeat) {
        var scope = course.getPopulationScope() != null ? course.getPopulationScope() : PopulationScope.FOLLOWED;
        KnowledgeEntity entity = switch (scope) {
            case FOLLOWED -> knowledgeRepository.findFirstNotSoNewItemFollowed(
                    course.getUser().getId(), course.getGameMode().getId(), lastPersonId, allowRepeat);
            case ALL -> knowledgeRepository.findFirstNotSoNewItemAll(
                    course.getUser().getId(), course.getGameMode().getId(), lastPersonId, allowRepeat);
        };
        return knowledgeEntityMapper.toModel(entity);
    }

    /** LEARNED: erreurs récentes */
    public Knowledge findFirstRecentError(Course course, Long lastPersonId, boolean allowRepeat) {
        var scope = course.getPopulationScope() != null ? course.getPopulationScope() : PopulationScope.FOLLOWED;
        KnowledgeEntity entity = switch (scope) {
            case FOLLOWED -> knowledgeRepository.findFirstRecentErrorFollowed(
                    course.getUser().getId(), course.getGameMode().getId(), lastPersonId, allowRepeat);
            case ALL -> knowledgeRepository.findFirstRecentErrorAll(
                    course.getUser().getId(), course.getGameMode().getId(), lastPersonId, allowRepeat);
        };
        return knowledgeEntityMapper.toModel(entity);
    }

    /** LEARNED: SRS dues */
    public Knowledge findFirstSRS(Course course, Long lastPersonId, boolean allowRepeat) {
        var scope = course.getPopulationScope() != null ? course.getPopulationScope() : PopulationScope.FOLLOWED;
        KnowledgeEntity entity = switch (scope) {
            case FOLLOWED -> knowledgeRepository.findFirstSrsDueFollowed(
                    course.getUser().getId(), course.getGameMode().getId(), lastPersonId, allowRepeat);
            case ALL -> knowledgeRepository.findFirstSrsDueAll(
                    course.getUser().getId(), course.getGameMode().getId(), lastPersonId, allowRepeat);
        };
        return knowledgeEntityMapper.toModel(entity);
    }

    /** MASTERED ou LEARNED future dues — random */
    public Knowledge findRevision(Course course, Long lastPersonId, boolean allowRepeat) {
        var scope = course.getPopulationScope() != null ? course.getPopulationScope() : PopulationScope.FOLLOWED;
        KnowledgeEntity entity = switch (scope) {
            case FOLLOWED -> knowledgeRepository.findRevisionFollowed(
                    course.getUser().getId(), course.getGameMode().getId(), lastPersonId, allowRepeat);
            case ALL -> knowledgeRepository.findRevisionAll(
                    course.getUser().getId(), course.getGameMode().getId(), lastPersonId, allowRepeat);
        };
        return knowledgeEntityMapper.toModel(entity);
    }

    // ----------------------------------------------------------------

    public void update(Knowledge knowledge) {
        knowledgeRepository.save(knowledgeEntityMapper.toEntity(knowledge));
    }

    public List<Knowledge> findAllByCourse(Course course) {
        return knowledgeRepository
                .findByGameModeIdAndUserIdAndStatusNot(
                        course.getGameMode().getId(),
                        course.getUser().getId(),
                        KnowledgeStatus.MASTERED)
                .stream()
                .map(knowledgeEntityMapper::toModel)
                .toList();
    }
}
