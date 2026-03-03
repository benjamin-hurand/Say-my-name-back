// src/main/java/com/saymyname/persistence/dao/course/KnowledgeDao.java
package com.saymyname.persistence.dao.course;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.course.KnowledgeCandidate;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.PopulationScope;
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
                return knowledgeRepository.findById(id).map(knowledgeEntityMapper::toModel);
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

        /** Lookup direct via factId (fallback stable). */
        public Optional<Knowledge> findByUserAndFact(Long userId, Long factId) {
                if (userId == null || factId == null) {
                        return Optional.empty();
                }
                return knowledgeRepository.findByUserIdAndFactId(userId, factId)
                                .map(knowledgeEntityMapper::toModel);
        }

        // ----------------------------------------------------------------
        // SEED / INSERT BATCH
        // ----------------------------------------------------------------

        public int insertBatchOfTenKnowledges(Course course) {
                return insertNextKnowledges(course, BATCH_SIZE);
        }

        /** Insert a lazy batch of UNKNOWN knowledges for this course. */
        public int insertNextKnowledges(Course course, int limit) {
                if (course == null || limit <= 0) {
                        return 0;
                }

                PopulationScope scope = course.getPopulationScope() != null
                                ? course.getPopulationScope()
                                : PopulationScope.FOLLOWED;

                long userId = course.getUser().getId();

                Long targetAttributeId = course.getTargetAttributeId();

                return switch (scope) {
                        case FOLLOWED -> knowledgeRepository.insertNextKnowledgesForCourseFollowed(
                                        userId,
                                        targetAttributeId,
                                        INITIAL_EF,
                                        INITIAL_DIFF,
                                        INITIAL_STABILITY,
                                        limit);
                        case ALL -> knowledgeRepository.insertNextKnowledgesForCourseAll(
                                        userId,
                                        targetAttributeId,
                                        INITIAL_EF,
                                        INITIAL_DIFF,
                                        INITIAL_STABILITY,
                                        limit);
                };
        }

        public int countByCourseAndStatus(Course course, KnowledgeStatus status) {
                if (course == null || course.getUser() == null || course.getUser().getId() == null) {
                        return 0;
                }
                return knowledgeRepository.countByUserIdAndStatusIn(
                                course.getUser().getId(),
                                status.scope());
        }

        // Nombre d’items SRS “dus” maintenant pour ce course (dépend du scope)
        public long countDueNow(Course course) {
                if (course == null || course.getUser() == null || course.getUser().getId() == null) {
                        return 0;
                }

                PopulationScope scope = course.getPopulationScope() != null
                                ? course.getPopulationScope()
                                : PopulationScope.FOLLOWED;

                boolean followed = (scope == PopulationScope.FOLLOWED);

                Long targetAttributeId = course.getTargetAttributeId();

                return knowledgeRepository.countSrsDue(
                                course.getUser().getId(),
                                targetAttributeId,
                                followed);
        }

        /** Upsert de la connaissance après calcul SM-2/PFA/FSRS. */
        public void upsertKnowledge(Knowledge k) {
                if (k == null || k.getUser() == null || k.getUser().getId() == null || k.getFactId() == null) {
                        throw new IllegalArgumentException("Knowledge upsert requires userId + factId");
                }

                knowledgeRepository.upsertKnowledge(
                                k.getUser().getId(),
                                k.getFactId(),
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
                                k.getDifficulty(),
                                k.isPendingRevalidation(),
                                k.getRevalidationReason());
        }

        // --------- POOLS (Candidates) -------------------

        private static PageRequest one() {
                return PageRequest.of(0, 1);
        }

        private static KnowledgeCandidate firstCandidateOrNull(List<KnowledgeCandidate> list) {
                return (list == null || list.isEmpty()) ? null : list.get(0);
        }

        /** UNKNOWN */
        @Transactional(readOnly = true)
        public KnowledgeCandidate findFirstNewCandidate(Course course, Long lastPersonId, boolean allowRepeat) {
                if (course == null || course.getUser() == null || course.getUser().getId() == null) {
                        return null;
                }

                var scope = course.getPopulationScope() != null ? course.getPopulationScope()
                                : PopulationScope.FOLLOWED;
                var userId = course.getUser().getId();

                var targetAttributeId = course.getTargetAttributeId();

                return switch (scope) {
                        case FOLLOWED -> firstCandidateOrNull(
                                        knowledgeRepository.findFirstNewItemFollowed(
                                                        userId,
                                                        targetAttributeId,
                                                        lastPersonId,
                                                        allowRepeat,
                                                        one()));
                        case ALL -> firstCandidateOrNull(
                                        knowledgeRepository.findFirstNewItemAll(
                                                        userId,
                                                        targetAttributeId,
                                                        lastPersonId,
                                                        allowRepeat,
                                                        one()));
                };
        }

        /** DISCOVERED */
        @Transactional(readOnly = true)
        public KnowledgeCandidate findFirstDiscoveredCandidate(Course course, Long lastPersonId, boolean allowRepeat) {
                if (course == null || course.getUser() == null || course.getUser().getId() == null) {
                        return null;
                }

                var scope = course.getPopulationScope() != null ? course.getPopulationScope()
                                : PopulationScope.FOLLOWED;
                var userId = course.getUser().getId();

                var targetAttributeId = course.getTargetAttributeId();

                return switch (scope) {
                        case FOLLOWED -> firstCandidateOrNull(
                                        knowledgeRepository.findFirstNotSoNewItemFollowed(
                                                        userId,
                                                        targetAttributeId,
                                                        lastPersonId,
                                                        allowRepeat,
                                                        one()));
                        case ALL -> firstCandidateOrNull(
                                        knowledgeRepository.findFirstNotSoNewItemAll(
                                                        userId,
                                                        targetAttributeId,
                                                        lastPersonId,
                                                        allowRepeat,
                                                        one()));
                };
        }

        /** LEARNED: erreurs récentes (since = now - 1 day) */
        @Transactional(readOnly = true)
        public KnowledgeCandidate findFirstRecentErrorCandidate(Course course, Long lastPersonId, boolean allowRepeat) {
                if (course == null || course.getUser() == null || course.getUser().getId() == null) {
                        return null;
                }

                var scope = course.getPopulationScope() != null ? course.getPopulationScope()
                                : PopulationScope.FOLLOWED;
                var userId = course.getUser().getId();

                var targetAttributeId = course.getTargetAttributeId();

                var since = LocalDateTime.now().minusDays(1);

                return switch (scope) {
                        case FOLLOWED -> firstCandidateOrNull(
                                        knowledgeRepository.findFirstRecentErrorFollowed(
                                                        userId,
                                                        targetAttributeId,
                                                        since,
                                                        lastPersonId,
                                                        allowRepeat,
                                                        one()));
                        case ALL -> firstCandidateOrNull(
                                        knowledgeRepository.findFirstRecentErrorAll(
                                                        userId,
                                                        targetAttributeId,
                                                        since,
                                                        lastPersonId,
                                                        allowRepeat,
                                                        one()));
                };
        }

        /** LEARNED: SRS dues */
        @Transactional(readOnly = true)
        public KnowledgeCandidate findFirstSRSCandidate(Course course, Long lastPersonId, boolean allowRepeat) {
                if (course == null || course.getUser() == null || course.getUser().getId() == null) {
                        return null;
                }

                var scope = course.getPopulationScope() != null ? course.getPopulationScope()
                                : PopulationScope.FOLLOWED;
                var userId = course.getUser().getId();

                var targetAttributeId = course.getTargetAttributeId();

                return switch (scope) {
                        case FOLLOWED -> firstCandidateOrNull(
                                        knowledgeRepository.findFirstSrsDueFollowed(
                                                        userId,
                                                        targetAttributeId,
                                                        lastPersonId,
                                                        allowRepeat,
                                                        one()));
                        case ALL -> firstCandidateOrNull(
                                        knowledgeRepository.findFirstSrsDueAll(
                                                        userId,
                                                        targetAttributeId,
                                                        lastPersonId,
                                                        allowRepeat,
                                                        one()));
                };
        }

        /** MASTERED ou LEARNED futures — random */
        @Transactional(readOnly = true)
        public KnowledgeCandidate findRevisionCandidate(Course course, Long lastPersonId, boolean allowRepeat) {
                if (course == null || course.getUser() == null || course.getUser().getId() == null) {
                        return null;
                }

                var scope = course.getPopulationScope() != null ? course.getPopulationScope()
                                : PopulationScope.FOLLOWED;
                var userId = course.getUser().getId();

                var targetAttributeId = course.getTargetAttributeId();

                var since = LocalDateTime.now().minusDays(1);

                return switch (scope) {
                        case FOLLOWED -> firstCandidateOrNull(
                                        knowledgeRepository.findRevisionFollowed(
                                                        userId,
                                                        targetAttributeId,
                                                        since,
                                                        lastPersonId,
                                                        allowRepeat,
                                                        one()));
                        case ALL -> firstCandidateOrNull(
                                        knowledgeRepository.findRevisionAll(
                                                        userId,
                                                        targetAttributeId,
                                                        since,
                                                        lastPersonId,
                                                        allowRepeat,
                                                        one()));
                };
        }

        // ----------------------------------------------------------------

        public void update(Knowledge knowledge) {
                knowledgeRepository.save(knowledgeEntityMapper.toEntity(knowledge));
        }

        @Transactional(readOnly = true)
        public List<KnowledgeCandidate> findNextDueMultiCandidates(
                        Course course,
                        Long primaryPersonId,
                        Long lastPersonId,
                        int limit,
                        int maxErrorStreak,
                        double maxAvgRtMs,
                        double maxHelpRecent,
                        double minAttemptsRecent,
                        int fetchFactor) {

                if (course == null || course.getUser() == null || course.getUser().getId() == null || limit <= 0) {
                        return List.of();
                }

                var scope = course.getPopulationScope() != null ? course.getPopulationScope()
                                : PopulationScope.FOLLOWED;
                var userId = course.getUser().getId();
                boolean followed = scope == PopulationScope.FOLLOWED;

                var targetAttributeId = course.getTargetAttributeId();

                int fetchLimit = Math.max(limit, limit * Math.max(1, fetchFactor));
                List<String> statuses = List.of(KnowledgeStatus.LEARNED.name(), KnowledgeStatus.MASTERED.name());

                return knowledgeRepository.findNextDueMultiRaw(
                                userId,
                                targetAttributeId,
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
                                .map(KnowledgeDao::mapRowToCandidate)
                                .toList();
        }

        private static KnowledgeCandidate mapRowToCandidate(Object[] row) {
                // order: knowledge_id, fact_id, person_id, attribute_id, status,
                // next_review_date
                Long knowledgeId = row[0] == null ? null : ((Number) row[0]).longValue();
                Long factId = row[1] == null ? null : ((Number) row[1]).longValue();
                Long personId = row[2] == null ? null : ((Number) row[2]).longValue();
                Long attributeId = row[3] == null ? null : ((Number) row[3]).longValue();
                KnowledgeStatus status = row[4] == null ? null : KnowledgeStatus.valueOf(row[4].toString());

                LocalDateTime nextReviewDate = null;
                Object dt = row[5];
                if (dt instanceof java.sql.Timestamp ts) {
                        nextReviewDate = ts.toLocalDateTime();
                } else if (dt instanceof LocalDateTime ldt) {
                        nextReviewDate = ldt;
                }

                return new KnowledgeCandidate(knowledgeId, factId, personId, attributeId, status, nextReviewDate);
        }

        public List<Knowledge> findAllByCourse(Course course) {
                if (course == null || course.getUser() == null || course.getUser().getId() == null) {
                        return List.of();
                }
                return knowledgeRepository.findByUserIdAndStatusNot(
                                course.getUser().getId(),
                                KnowledgeStatus.MASTERED)
                                .stream()
                                .map(knowledgeEntityMapper::toModel)
                                .toList();
        }

        public int resetForCourseScope(
                        long userId,
                        Long targetAttributeId,
                        PopulationScope popScope,
                        double baselineEase, double baselineDiff, double baselineStability) {
                return knowledgeRepository.resetForCourseScope(
                                userId,
                                targetAttributeId,
                                popScope.name(),
                                KnowledgeStatus.UNKNOWN,
                                baselineEase, baselineDiff, baselineStability);
        }

        public long countToResetForCourseScope(long userId, Long targetAttributeId,
                        PopulationScope popScope) {
                return knowledgeRepository.countToResetForCourseScope(
                                userId,
                                targetAttributeId,
                                popScope.name());
        }
}