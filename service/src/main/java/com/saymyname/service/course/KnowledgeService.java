// src/main/java/com/saymyname/service/course/KnowledgeService.java
package com.saymyname.service.course;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionAttempt;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.course.KnowledgeCandidate;
import com.saymyname.core.model.course.KnowledgeResultEvent;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.core.model.leaderboard.XpAward;
import com.saymyname.core.model.people.Fact;
import com.saymyname.persistence.dao.FactDao;
import com.saymyname.persistence.dao.course.KnowledgeDao;
import com.saymyname.service.course.scheduler.SchedulerStrategy;
import com.saymyname.service.leaderboard.LeaderboardService;

@Service
public class KnowledgeService {

    private static final BigDecimal INITIAL_EF = BigDecimal.valueOf(2.5);
    private static final double INITIAL_DIFF = 1.0;
    private static final double INITIAL_STABILITY = 1.0;

    private static final String EK_STREAK_5 = "KNOWLEDGE_STREAK_MILESTONE_5";
    private static final String EK_STREAK_10 = "KNOWLEDGE_STREAK_MILESTONE_10";
    private static final String EK_STREAK_20 = "KNOWLEDGE_STREAK_MILESTONE_20";

    private static final String SOURCE_TYPE_KNOWLEDGE = "KNOWLEDGE";

    private final KnowledgeDao knowledgeDao;
    private final Map<SrsAlgorithm, SchedulerStrategy> strategies;
    private final SrsAlgorithm defaultAlgorithm;
    private final FactDao factDao;
    private final LeaderboardService leaderboardService;

    public KnowledgeService(
            KnowledgeDao knowledgeDao,
            Map<SrsAlgorithm, SchedulerStrategy> strategies,
            @Value("${quiz.reviewAlgorithm:SM2}") SrsAlgorithm defaultAlgorithm,
            FactDao factDao,
            LeaderboardService leaderboardService) {
        this.knowledgeDao = Objects.requireNonNull(knowledgeDao, "knowledgeDao");
        this.strategies = Objects.requireNonNull(strategies, "strategies");
        this.defaultAlgorithm = Objects.requireNonNull(defaultAlgorithm, "defaultAlgorithm");
        this.factDao = Objects.requireNonNull(factDao, "factDao");
        this.leaderboardService = Objects.requireNonNull(leaderboardService, "leaderboardService");
    }

    public int insertBatchOfTenKnowledges(Course course) {
        return knowledgeDao.insertBatchOfTenKnowledges(course);
    }

    public int insertNextKnowledges(Course course, int limit) {
        return knowledgeDao.insertNextKnowledges(course, limit);
    }

    public int ensureSeed(Course course, int limit) {
        if (course == null || limit <= 0) {
            return 0;
        }
        return knowledgeDao.insertNextKnowledges(course, limit);
    }

    public void update(Knowledge knowledge) {
        knowledgeDao.update(knowledge);
    }

    public int countByCourseAndStatus(Course course, KnowledgeStatus status) {
        return knowledgeDao.countByCourseAndStatus(course, status);
    }

    // ---------------------------------------------------------------------
    // ✅ Validation pure, basée sur snapshot
    // ---------------------------------------------------------------------

    @Transactional
    public XpAward recordCourseAnswerResults(
            User user,
            Course course,
            CourseQuestionAttempt attempt,
            boolean helpUsed,
            List<KnowledgeResultEvent> events) {
        return recordBatchResults(user, events);
    }

    @Transactional
    public XpAward recordBatchResults(User user, List<KnowledgeResultEvent> events) {
        if (user == null || user.getId() == null) {
            return XpAward.none();
        }
        if (events == null || events.isEmpty()) {
            return XpAward.none();
        }

        var grouped = events.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(KnowledgeService::toEventKey));

        Map<Long, Knowledge> knowledgeById = preloadKnowledgesById(user, grouped.keySet());

        long totalDeltaXp = 0;
        List<String> eventKeys = new ArrayList<>(8);

        for (var entry : grouped.entrySet()) {
            EventKey key = entry.getKey();
            List<KnowledgeResultEvent> groupEvents = entry.getValue();
            if (groupEvents == null || groupEvents.isEmpty()) {
                continue;
            }

            Knowledge knowledge = loadKnowledgeForKey(user, key, groupEvents, knowledgeById);

            long totalAnswerXp = 0;
            List<StreakMilestoneHit> streakHits = new ArrayList<>();

            int prevGlobalStreak = knowledge.getGlobalStreak();
            boolean hasAnyCountedAnswer = false;

            for (KnowledgeResultEvent ev : groupEvents) {
                boolean correct = ev.isCorrect();
                boolean helpUsed = ev.isHelpUsed();

                applyResultToKnowledge(knowledge, user, correct, helpUsed);

                int xp = leaderboardService.computeXpForKnowledgeResult(correct, helpUsed);
                totalAnswerXp += xp;

                if (!(correct && helpUsed)) {
                    hasAnyCountedAnswer = true;
                }

                int newGlobalStreak = knowledge.getGlobalStreak();
                if (newGlobalStreak != prevGlobalStreak) {
                    int bonus = leaderboardService.computeStreakBonus(newGlobalStreak);
                    if (bonus > 0) {
                        streakHits.add(new StreakMilestoneHit(newGlobalStreak, bonus));
                    }
                    prevGlobalStreak = newGlobalStreak;
                }
            }

            // Persist knowledge first
            knowledgeDao.upsertKnowledge(knowledge);

            // XP attribution : tu dois donner un ref “personId” au leaderboard.
            // Ici, on prend ce qui est fourni par l’event (snapshot), sinon null.
            Long refPersonId = resolvePersonIdForXpFromFact(knowledge, groupEvents);
            LocalDateTime at = LocalDateTime.now();

            if (totalAnswerXp > 0 && refPersonId != null) {
                int delta = safeLongToInt(totalAnswerXp);
                totalDeltaXp += delta;
                eventKeys.add("KNOWLEDGE_ANSWER_BATCH");

                leaderboardService.addXp(
                        user,
                        "KNOWLEDGE_ANSWER_BATCH",
                        SOURCE_TYPE_KNOWLEDGE,
                        refPersonId,
                        delta,
                        at,
                        false,
                        !hasAnyCountedAnswer);
            }

            for (StreakMilestoneHit hit : streakHits) {
                if (refPersonId == null) {
                    continue;
                }
                int bonus = safeLongToInt(hit.bonusXp());
                if (bonus <= 0) {
                    continue;
                }

                String ek = computeStreakEventKey(hit.milestone());
                totalDeltaXp += bonus;
                eventKeys.add(ek);

                leaderboardService.addXp(
                        user,
                        ek,
                        SOURCE_TYPE_KNOWLEDGE,
                        refPersonId,
                        bonus,
                        at,
                        false,
                        true);
            }
        }

        return new XpAward(safeLongToInt(totalDeltaXp), List.copyOf(eventKeys));
    }

    // ---------------------------------------------------------------------
    // EventKey = knowledgeId (hot) sinon factId (fallback stable)
    // ---------------------------------------------------------------------

    private static EventKey toEventKey(KnowledgeResultEvent ev) {
        if (ev.getKnowledgeId() != null) {
            return new KnowledgeIdKey(ev.getKnowledgeId());
        }
        if (ev.getFactId() != null) {
            return new FactIdKey(ev.getFactId());
        }
        throw new IllegalArgumentException("KnowledgeResultEvent missing both knowledgeId and factId");
    }

    private Knowledge loadKnowledgeForKey(
            User user,
            EventKey key,
            List<KnowledgeResultEvent> groupEvents,
            Map<Long, Knowledge> knowledgeById) {

        if (key instanceof KnowledgeIdKey kid) {
            Knowledge knowledge = knowledgeById != null ? knowledgeById.get(kid.knowledgeId()) : null;
            if (knowledge == null) {
                knowledge = knowledgeDao.findByIdForUser(user.getId(), kid.knowledgeId()).orElse(null);
            }
            if (knowledge == null) {
                throw new IllegalArgumentException(
                        "Knowledge not found for user/tenant by knowledgeId=" + kid.knowledgeId()
                                + ", userId=" + user.getId());
            }
            return knowledge;
        }

        FactIdKey fk = (FactIdKey) key;

        return knowledgeDao
                .findByUserAndFact(user.getId(), fk.factId())
                .orElseGet(() -> new Knowledge.Builder()
                        .withUser(user)
                        .withFactId(fk.factId())
                        .withStatus(KnowledgeStatus.DISCOVERED)
                        .withNextReviewDate(LocalDateTime.now())
                        .withLastReviewDate(LocalDateTime.now())
                        .withTotalRepetitionCount(0)
                        .withFailureCount(0)
                        .withSuccessCount(0)
                        .withSrsStreak(0)
                        .withGlobalStreak(0)
                        .withEaseFactor(INITIAL_EF)
                        .withDifficulty(INITIAL_DIFF)
                        .withStability(INITIAL_STABILITY)
                        .withPendingRevalidation(false)
                        .withRevalidationReason(null)
                        .build());
    }

    private Map<Long, Knowledge> preloadKnowledgesById(User user, Set<EventKey> keys) {
        if (user == null || user.getId() == null || keys == null || keys.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = keys.stream()
                .filter(k -> k instanceof KnowledgeIdKey)
                .map(k -> ((KnowledgeIdKey) k).knowledgeId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return knowledgeDao.findAllByIdsForUser(user.getId(), ids).stream()
                .filter(Objects::nonNull)
                .filter(k -> k.getId() != null)
                .collect(Collectors.toMap(Knowledge::getId, k -> k, (a, b) -> a));
    }

    private Long resolvePersonIdForXpFromFact(Knowledge knowledge, List<KnowledgeResultEvent> groupEvents) {
        // 1) knowledge.factId (best)
        Long factId = (knowledge != null) ? knowledge.getFactId() : null;

        // 2) fallback: event.factId
        if (factId == null && groupEvents != null && !groupEvents.isEmpty()) {
            for (KnowledgeResultEvent ev : groupEvents) {
                if (ev != null && ev.getFactId() != null) {
                    factId = ev.getFactId();
                    break;
                }
            }
        }

        if (factId == null)
            return null;

        return factDao.findById(factId)
                .map(Fact::getPersonId)
                .orElse(null);
    }

    private sealed interface EventKey permits KnowledgeIdKey, FactIdKey {
    }

    private record KnowledgeIdKey(Long knowledgeId) implements EventKey {
    }

    private record FactIdKey(Long factId) implements EventKey {
    }

    private static int safeLongToInt(long value) {
        if (value <= 0)
            return 0;
        if (value > Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        return (int) value;
    }

    private static String computeStreakEventKey(int globalStreak) {
        return switch (globalStreak) {
            case 5 -> EK_STREAK_5;
            case 10 -> EK_STREAK_10;
            case 20 -> EK_STREAK_20;
            default -> "KNOWLEDGE_STREAK_MILESTONE_" + globalStreak;
        };
    }

    private record StreakMilestoneHit(int milestone, int bonusXp) {
    }

    private void applyResultToKnowledge(
            Knowledge k,
            User user,
            boolean isCorrect,
            boolean helpUsed) {

        LocalDateTime now = LocalDateTime.now();

        if (!(isCorrect && helpUsed)) {
            k.setTotalRepetitionCount(k.getTotalRepetitionCount() + 1);

            if (isCorrect) {
                k.setSuccessCount(k.getSuccessCount() + 1);
                k.setGlobalStreak(Math.max(1, k.getGlobalStreak() + 1));
            } else {
                k.setFailureCount(k.getFailureCount() + 1);
                k.setGlobalStreak(Math.min(-1, k.getGlobalStreak() - 1));
            }

            boolean srsDue = k.getNextReviewDate() == null || !k.getNextReviewDate().isAfter(now);
            SrsAlgorithm algo = user.getSrsAlgorithm() != null ? user.getSrsAlgorithm() : defaultAlgorithm;
            SchedulerStrategy scheduler = strategies.get(algo);
            int grade = scheduler.mapGrade(isCorrect);

            switch (algo) {
                case SM2 -> {
                    if (!isCorrect || srsDue) {
                        scheduler.schedule(k, grade);
                    }
                }
                case PFA, FSRS -> scheduler.schedule(k, grade);
            }
        } else {
            k.setGlobalStreak(0);
            k.setSrsStreak(0);
        }

        KnowledgeStatus status = k.getStatus();
        if (status == KnowledgeStatus.UNKNOWN)
            status = KnowledgeStatus.DISCOVERED;
        if (isCorrect && !helpUsed && status == KnowledgeStatus.DISCOVERED)
            status = KnowledgeStatus.LEARNED;
        k.setStatus(status);

        k.setLastReviewDate(now);
    }

    // ---------------------------------------------------------------------
    // POOLS (Candidates) => pour l'orchestration / build question
    // ---------------------------------------------------------------------

    public KnowledgeCandidate findFirstNewCandidate(Course course, Long lastPersonId, boolean allowRepeat) {
        return knowledgeDao.findFirstNewCandidate(course, lastPersonId, allowRepeat);
    }

    public KnowledgeCandidate findFirstDiscoveredCandidate(Course course, Long lastPersonId, boolean allowRepeat) {
        return knowledgeDao.findFirstDiscoveredCandidate(course, lastPersonId, allowRepeat);
    }

    public KnowledgeCandidate findFirstRecentErrorCandidate(Course course, Long lastPersonId, boolean allowRepeat) {
        return knowledgeDao.findFirstRecentErrorCandidate(course, lastPersonId, allowRepeat);
    }

    public KnowledgeCandidate findFirstSRSCandidate(Course course, Long lastPersonId, boolean allowRepeat) {
        return knowledgeDao.findFirstSRSCandidate(course, lastPersonId, allowRepeat);
    }

    public KnowledgeCandidate findRevisionCandidate(Course course, Long lastPersonId, boolean allowRepeat) {
        return knowledgeDao.findRevisionCandidate(course, lastPersonId, allowRepeat);
    }

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
        return knowledgeDao.findNextDueMultiCandidates(
                course,
                primaryPersonId,
                lastPersonId,
                limit,
                maxErrorStreak,
                maxAvgRtMs,
                maxHelpRecent,
                minAttemptsRecent,
                fetchFactor);
    }

    // ---------------------------------------------------------------------
    // Rest (unchanged)
    // ---------------------------------------------------------------------

    public long countDueNow(Course course) {
        return knowledgeDao.countDueNow(course);
    }

    public List<Knowledge> findAllByCourse(Course course) {
        return knowledgeDao.findAllByCourse(course);
    }

    public int resetForCourseScope(
            long userId,
            Long targetAttributeId,
            PopulationScope popScope,
            double baselineEase,
            double baselineDiff,
            double baselineStability) {
        return knowledgeDao.resetForCourseScope(userId, targetAttributeId, popScope, baselineEase,
                baselineDiff,
                baselineStability);
    }

    public long countToResetForCourseScope(long userId, Long targetAttributeId,
            PopulationScope popScope) {
        return knowledgeDao.countToResetForCourseScope(userId, targetAttributeId, popScope);
    }
}