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
import com.saymyname.core.model.course.KnowledgeResultEvent;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.core.model.leaderboard.XpAward;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.quiz.options.GameMode;
import com.saymyname.persistence.dao.FactDao;
import com.saymyname.persistence.dao.course.KnowledgeDao;
import com.saymyname.service.course.scheduler.SchedulerStrategy;
import com.saymyname.service.leaderboard.LeaderboardService;

@Service
public class KnowledgeService {

    private static final BigDecimal INITIAL_EF = BigDecimal.valueOf(2.5);
    private static final double INITIAL_DIFF = 1.0;
    private static final double INITIAL_STABILITY = 1.0;

    // --- Event keys (stables) ---
    private static final String EK_STREAK_5 = "KNOWLEDGE_STREAK_MILESTONE_5";
    private static final String EK_STREAK_10 = "KNOWLEDGE_STREAK_MILESTONE_10";
    private static final String EK_STREAK_20 = "KNOWLEDGE_STREAK_MILESTONE_20";

    private static final String SOURCE_TYPE_KNOWLEDGE = "KNOWLEDGE";

    private final KnowledgeDao knowledgeDao;
    private final Map<SrsAlgorithm, SchedulerStrategy> strategies;
    private final SrsAlgorithm defaultAlgorithm;
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
        this.leaderboardService = Objects.requireNonNull(leaderboardService, "leaderboardService");
    }

    public int insertBatchOfTenKnowledges(Course course) {
        return knowledgeDao.insertBatchOfTenKnowledges(course);
    }

    public int insertNextKnowledges(Course course, int limit) {
        return knowledgeDao.insertNextKnowledges(course, limit);
    }

    /**
     * Lazy seed for course knowledge pool.
     * Returns how many new UNKNOWN knowledges were inserted.
     */
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

    public Knowledge findByUserGameModeAndPerson(Long userId, Long gameModeId, Long personId) {
        if (userId == null || gameModeId == null || personId == null) {
            return null;
        }
        return knowledgeDao.findByUserGameModeAndPerson(userId, gameModeId, personId).orElse(null);
    }

    // ---------------------------------------------------------------------
    // ✅ NEW: Validation pure, basée sur la vérité figée dans le snapshot
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

    // ---------------------------------------------------------------------
    // RecordBatchResults inchangé (SRS/XP)
    // ---------------------------------------------------------------------

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

            // Persist knowledge first (unchanged behavior)
            knowledgeDao.upsertKnowledge(knowledge);

            Long refPersonId = resolvePersonIdForXp(key, knowledge, groupEvents);
            LocalDateTime at = LocalDateTime.now();

            // --- XP for answer batch (unchanged write behavior) ---
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

            // --- XP for streak bonuses (unchanged write behavior) ---
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

    private static EventKey toEventKey(KnowledgeResultEvent ev) {
        if (ev.getKnowledgeId() != null) {
            return new KnowledgeIdKey(ev.getKnowledgeId());
        }
        Long gm = ev.getGameModeId();
        Long pid = ev.getPersonId();
        if (gm == null || pid == null) {
            throw new IllegalArgumentException(
                    "KnowledgeResultEvent missing both knowledgeId and (gameModeId, personId)");
        }
        return new ModePersonKey(gm, pid);
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
                        "Knowledge not found for user/org by knowledgeId=" + kid.knowledgeId()
                                + ", userId=" + user.getId());
            }
            return knowledge;
        }

        ModePersonKey mp = (ModePersonKey) key;

        return knowledgeDao
                .findByUserGameModeAndPerson(user.getId(), mp.gameModeId(), mp.personId())
                .orElseGet(() -> new Knowledge.Builder()
                        .withUser(user)
                        .withGameMode(new GameMode.Builder().withId(mp.gameModeId()).build())
                        .withPerson(new Person.Builder().withId(mp.personId()).build())
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

    private static Long resolvePersonIdForXp(EventKey key, Knowledge knowledge,
            List<KnowledgeResultEvent> groupEvents) {
        if (key instanceof ModePersonKey mp) {
            return mp.personId();
        }
        if (knowledge != null && knowledge.getPerson() != null && knowledge.getPerson().getId() != null) {
            return knowledge.getPerson().getId();
        }
        if (groupEvents != null && !groupEvents.isEmpty()) {
            KnowledgeResultEvent first = groupEvents.get(0);
            return first.getPersonId();
        }
        return null;
    }

    private sealed interface EventKey permits KnowledgeIdKey, ModePersonKey {
    }

    private record KnowledgeIdKey(Long knowledgeId) implements EventKey {
    }

    private record ModePersonKey(Long gameModeId, Long personId) implements EventKey {
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

    // POOLS (inchangé)
    public Knowledge findFirstNew(Course course, Long lastPersonId, boolean allowRepeat) {
        return knowledgeDao.findFirstNew(course, lastPersonId, allowRepeat);
    }

    public Knowledge findFirstDiscovered(Course course, Long lastPersonId, boolean allowRepeat) {
        return knowledgeDao.findFirstDiscovered(course, lastPersonId, allowRepeat);
    }

    public Knowledge findFirstRecentError(Course course, Long lastPersonId, boolean allowRepeat) {
        return knowledgeDao.findFirstRecentError(course, lastPersonId, allowRepeat);
    }

    public Knowledge findFirstSRS(Course course, Long lastPersonId, boolean allowRepeat) {
        return knowledgeDao.findFirstSRS(course, lastPersonId, allowRepeat);
    }

    public Knowledge findRevision(Course course, Long lastPersonId, boolean allowRepeat) {
        return knowledgeDao.findRevision(course, lastPersonId, allowRepeat);
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
        return knowledgeDao.findNextDueMulti(
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

    public long countDueNow(Course course) {
        return knowledgeDao.countDueNow(course);
    }

    public List<Knowledge> findAllByCourse(Course course) {
        return knowledgeDao.findAllByCourse(course);
    }

    public int resetForCourseScope(
            long userId,
            long gameModeId,
            PopulationScope popScope,
            double baselineEase,
            double baselineDiff,
            double baselineStability) {
        return knowledgeDao.resetForCourseScope(userId, gameModeId, popScope, baselineEase, baselineDiff,
                baselineStability);
    }

    public long countToResetForCourseScope(long userId, long gameModeId, PopulationScope popScope) {
        return knowledgeDao.countToResetForCourseScope(userId, gameModeId, popScope);
    }
}
