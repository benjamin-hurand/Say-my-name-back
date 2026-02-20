package com.saymyname.persistence.dao.course;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.persistence.entity.organization.course.KnowledgeEntity;
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

        public Optional<Knowledge> findById(Long id) {
                Optional<KnowledgeEntity> entity = knowledgeRepository.findById(id);
                return entity.map(knowledgeEntityMapper::toModel);
        }

        public Optional<Knowledge> findByIdForUser(Long userId, Long knowledgeId) {
                if (userId == null || knowledgeId == null) {
                        return Optional.empty();
                }
                return knowledgeRepository.findByIdForUser(userId, knowledgeId)
                                .map(knowledgeEntityMapper::toModel);
        }

        public List<Knowledge> findAllByIdsForUser(Long userId, Collection<Long> ids) {
                if (userId == null || ids == null || ids.isEmpty()) {
                        return List.of();
                }
                return knowledgeRepository.findAllByIdsForUser(userId, ids).stream()
                                .map(knowledgeEntityMapper::toModel)
                                .toList();
        }

        /** Insère un batch d’UNKNOWN selon le scope du course (FOLLOWED / ALL) */
        public int insertBatchOfTenKnowledges(Course course) {
                return insertNextKnowledges(course, BATCH_SIZE);
        }

        /** Insert a lazy batch of UNKNOWN knowledges for this course. */
        public int insertNextKnowledges(Course course, int limit) {
                if (course == null || limit <= 0) {
                        return 0;
                }
                PopulationScope scope = course.getPopulationScope() != null ? course.getPopulationScope()
                                : PopulationScope.FOLLOWED;
                return switch (scope) {
                        case FOLLOWED -> knowledgeRepository.insertNextKnowledgesForCourseFollowed(
                                        course.getUser().getId(),
                                        course.getGameMode().getId(),
                                        INITIAL_EF,
                                        INITIAL_DIFF,
                                        INITIAL_STABILITY,
                                        limit);
                        case ALL -> knowledgeRepository.insertNextKnowledgesForCourseAll(
                                        course.getUser().getId(),
                                        course.getGameMode().getId(),
                                        INITIAL_EF,
                                        INITIAL_DIFF,
                                        INITIAL_STABILITY,
                                        limit);
                };
        }

        public int countByCourseAndStatus(Course course, KnowledgeStatus status) {
                return knowledgeRepository.countByUserIdAndGameModeIdAndStatusIn(
                                course.getUser().getId(),
                                course.getGameMode().getId(),
                                status.scope());
        }

        // Nombre d’items SRS “dus” maintenant pour ce course (dépend du scope)
        public long countDueNow(Course course) {
                PopulationScope scope = course.getPopulationScope() != null
                                ? course.getPopulationScope()
                                : PopulationScope.FOLLOWED;

                Long userId = course.getUser().getId();
                Long gameModeId = course.getGameMode().getId();

                boolean followed = (scope == PopulationScope.FOLLOWED);
                return knowledgeRepository.countSrsDue(userId, gameModeId, followed);
        }

        /** Récupère une connaissance précise sur une personne précise. */
        public Optional<Knowledge> findByUserGameModeAndPerson(Long userId, Long gameModeId, Long personId) {
                Optional<KnowledgeEntity> entity = knowledgeRepository.findByUserIdAndGameModeIdAndPersonId(userId,
                                gameModeId,
                                personId);
                return entity.map(knowledgeEntityMapper::toModel);
        }

        /** Upsert de la connaissance après calcul SM-2/PFA/FSRS. */
        public void upsertKnowledge(Knowledge k) {
                knowledgeRepository.upsertKnowledge(
                                k.getUser().getId(),
                                k.getGameMode().getId(),
                                k.getPerson().getId(),
                                toLocalDateTime(k.getNextReviewDate()),
                                k.getTotalRepetitionCount(),
                                k.getSrsStreak(),
                                k.getGlobalStreak(),
                                k.getEaseFactor(),
                                k.getStatus().name(),
                                toLocalDateTime(k.getLastReviewDate()),
                                k.getSuccessCount(),
                                k.getFailureCount(),
                                k.getStability(),
                                k.getDifficulty());
        }

        private static LocalDateTime toLocalDateTime(java.time.Instant value) {
                return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
        }

        // --------- POOLS : signatures DAO inchangées -------------------

        private static PageRequest one() {
                return PageRequest.of(0, 1);
        }

        /** Renvoie le premier Knowledge de la liste, ou null si vide. */
        private static Knowledge mapFirstOrNull(List<KnowledgeEntity> list, KnowledgeEntityMapper mapper) {
                return list.stream().findFirst().map(mapper::toModel).orElse(null);
        }

        /** UNKNOWN */
        public Knowledge findFirstNew(Course course, Long lastPersonId, boolean allowRepeat) {
                var scope = course.getPopulationScope() != null ? course.getPopulationScope()
                                : PopulationScope.FOLLOWED;
                var userId = course.getUser().getId();
                var gameModeId = course.getGameMode().getId();
                return switch (scope) {
                        case FOLLOWED -> mapFirstOrNull(
                                        knowledgeRepository.findFirstNewItemFollowed(userId, gameModeId, lastPersonId,
                                                        allowRepeat, one()),
                                        knowledgeEntityMapper);
                        case ALL -> mapFirstOrNull(
                                        knowledgeRepository.findFirstNewItemAll(userId, gameModeId, lastPersonId,
                                                        allowRepeat, one()),
                                        knowledgeEntityMapper);
                };
        }

        /** DISCOVERED */
        public Knowledge findFirstDiscovered(Course course, Long lastPersonId, boolean allowRepeat) {
                var scope = course.getPopulationScope() != null ? course.getPopulationScope()
                                : PopulationScope.FOLLOWED;
                var userId = course.getUser().getId();
                var gameModeId = course.getGameMode().getId();
                return switch (scope) {
                        case FOLLOWED -> mapFirstOrNull(
                                        knowledgeRepository.findFirstNotSoNewItemFollowed(userId, gameModeId,
                                                        lastPersonId, allowRepeat, one()),
                                        knowledgeEntityMapper);
                        case ALL -> mapFirstOrNull(
                                        knowledgeRepository.findFirstNotSoNewItemAll(userId, gameModeId, lastPersonId,
                                                        allowRepeat, one()),
                                        knowledgeEntityMapper);
                };
        }

        /** LEARNED: erreurs récentes (since = now - 1 day) */
        public Knowledge findFirstRecentError(Course course, Long lastPersonId, boolean allowRepeat) {
                var scope = course.getPopulationScope() != null ? course.getPopulationScope()
                                : PopulationScope.FOLLOWED;
                var userId = course.getUser().getId();
                var gameModeId = course.getGameMode().getId();
                var since = LocalDateTime.now().minusDays(1);
                return switch (scope) {
                        case FOLLOWED -> mapFirstOrNull(
                                        knowledgeRepository.findFirstRecentErrorFollowed(userId, gameModeId, since,
                                                        lastPersonId, allowRepeat, one()),
                                        knowledgeEntityMapper);
                        case ALL -> mapFirstOrNull(
                                        knowledgeRepository.findFirstRecentErrorAll(userId, gameModeId, since,
                                                        lastPersonId, allowRepeat, one()),
                                        knowledgeEntityMapper);
                };
        }

        /** LEARNED: SRS dues */
        public Knowledge findFirstSRS(Course course, Long lastPersonId, boolean allowRepeat) {
                var scope = course.getPopulationScope() != null ? course.getPopulationScope()
                                : PopulationScope.FOLLOWED;
                var userId = course.getUser().getId();
                var gameModeId = course.getGameMode().getId();
                return switch (scope) {
                        case FOLLOWED -> mapFirstOrNull(
                                        knowledgeRepository.findFirstSrsDueFollowed(userId, gameModeId, lastPersonId,
                                                        allowRepeat, one()),
                                        knowledgeEntityMapper);
                        case ALL -> mapFirstOrNull(
                                        knowledgeRepository.findFirstSrsDueAll(userId, gameModeId, lastPersonId,
                                                        allowRepeat, one()),
                                        knowledgeEntityMapper);
                };
        }

        /** MASTERED ou LEARNED futures — random (order by rand) */
        public Knowledge findRevision(Course course, Long lastPersonId, boolean allowRepeat) {
                var scope = course.getPopulationScope() != null ? course.getPopulationScope()
                                : PopulationScope.FOLLOWED;
                var userId = course.getUser().getId();
                var gameModeId = course.getGameMode().getId();
                var since = LocalDateTime.now().minusDays(1);
                return switch (scope) {
                        case FOLLOWED -> mapFirstOrNull(
                                        knowledgeRepository.findRevisionFollowed(userId, gameModeId, since,
                                                        lastPersonId, allowRepeat, one()),
                                        knowledgeEntityMapper);
                        case ALL -> mapFirstOrNull(
                                        knowledgeRepository.findRevisionAll(userId, gameModeId, since, lastPersonId,
                                                        allowRepeat, one()),
                                        knowledgeEntityMapper);
                };
        }

        // ----------------------------------------------------------------

        public void update(Knowledge knowledge) {
                knowledgeRepository.save(knowledgeEntityMapper.toEntity(knowledge));
        }

        public List<Knowledge> findNextDueMulti(
                        Course course,
                        Long primaryPersonId,
                        Long lastPersonId,
                        int limit,
                        int maxErrorStreak,
                        double maxAvgRtMs,
                        double maxHelpRecent,
                        double minAttemptsRecent,
                        int fetchFactor) {
                if (course == null || limit <= 0)
                        return List.of();

                var scope = course.getPopulationScope() != null ? course.getPopulationScope()
                                : PopulationScope.FOLLOWED;
                var userId = course.getUser().getId();
                var gameModeId = course.getGameMode().getId();
                boolean followed = scope == PopulationScope.FOLLOWED;

                int fetchLimit = Math.max(limit, limit * Math.max(1, fetchFactor));
                List<String> statuses = List.of(KnowledgeStatus.LEARNED.name(), KnowledgeStatus.MASTERED.name());

                return knowledgeRepository.findNextDueMulti(
                                userId,
                                gameModeId,
                                primaryPersonId,
                                lastPersonId,
                                statuses,
                                followed,
                                maxErrorStreak,
                                maxAvgRtMs,
                                maxHelpRecent,
                                minAttemptsRecent,
                                fetchLimit)
                                .stream()
                                .map(knowledgeEntityMapper::toModel)
                                .toList();
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

        public int resetForCourseScope(long userId, long gameModeId, PopulationScope popScope,
                        double baselineEase, double baselineDiff, double baselineStability) {
                return knowledgeRepository.resetForCourseScope(
                                userId, gameModeId, popScope.name(),
                                KnowledgeStatus.UNKNOWN,
                                baselineEase, baselineDiff, baselineStability);
        }

        public long countToResetForCourseScope(long userId, long gameModeId, PopulationScope popScope) {
                return knowledgeRepository.countToResetForCourseScope(userId, gameModeId, popScope.name());
        }
}
