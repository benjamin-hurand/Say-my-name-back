// src/main/java/com/saymyname/service/course/KnowledgeService.java
package com.saymyname.service.course;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.course.AnswerValidationResult;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.course.KnowledgeResultEvent;
import com.saymyname.core.model.course.ResultAttribute;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.core.model.game.options.GameMode;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.core.util.AnswerValidator;
import com.saymyname.persistence.dao.PersonAttributeDao;
import com.saymyname.persistence.dao.course.KnowledgeDao;
import com.saymyname.service.course.scheduler.SchedulerStrategy;
import com.saymyname.service.leaderboard.LeaderboardService;

@Service
public class KnowledgeService {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeService.class);

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
    private final PersonAttributeDao personAttributeDao;
    private final LeaderboardService leaderboardService;

    public KnowledgeService(
            KnowledgeDao knowledgeDao,
            Map<SrsAlgorithm, SchedulerStrategy> strategies,
            @Value("${quiz.reviewAlgorithm:SM2}") SrsAlgorithm defaultAlgorithm,
            PersonAttributeDao personAttributeDao,
            LeaderboardService leaderboardService) {
        this.knowledgeDao = knowledgeDao;
        this.strategies = strategies;
        this.defaultAlgorithm = defaultAlgorithm;
        this.personAttributeDao = personAttributeDao;
        this.leaderboardService = leaderboardService;
    }

    public int insertBatchOfTenKnowledges(Course course) {
        return knowledgeDao.insertBatchOfTenKnowledges(course);
    }

    public void update(Knowledge knowledge) {
        knowledgeDao.update(knowledge);
    }

    public int countByCourseAndStatus(Course course, KnowledgeStatus status) {
        return knowledgeDao.countByCourseAndStatus(course, status);
    }

    /**
     * Enregistre un seul résultat pour une personne / gameMode.
     */
    @Transactional
    public void recordSingleResult(
            User user,
            Long gameModeId,
            Long personId,
            boolean correct,
            boolean helpUsed) {
        KnowledgeResultEvent ev = new KnowledgeResultEvent(gameModeId, personId, correct, helpUsed);
        recordBatchResults(user, List.of(ev));
    }

    @Transactional
    public AnswerValidationResult validateAnswer(
            Long personId,
            String answer,
            User user,
            GameMode gameMode,
            boolean helpUsed) {

        // 1) Calculer le verdict
        List<PersonAttribute> personAttrs = personAttributeDao.findAttributesByPersonId(personId);
        List<Long> modeAttrIds = gameMode.getGameModeAttributes().stream()
                .map(gma -> gma.getAttribute().getId())
                .toList();

        List<String> correctValues = personAttrs.stream()
                .filter(pa -> modeAttrIds.contains(pa.getAttribute().getId()))
                .map(PersonAttribute::getValue)
                .toList();

        boolean isCorrect = AnswerValidator.match(answer, correctValues, gameMode.getOperator(), true);
        String correctAnswer = String.join(" ", correctValues);

        // 2) Construire la liste des ResultAttribute
        List<ResultAttribute> resultAttrs = personAttrs.stream()
                .map(pa -> {
                    boolean isTarget = modeAttrIds.contains(pa.getAttribute().getId());
                    boolean isCorrectAttr = true;
                    if (isTarget) {
                        String normAnswer = answer == null ? "" : answer.trim().toLowerCase();
                        String normVal = pa.getValue() == null ? "" : pa.getValue().trim().toLowerCase();
                        isCorrectAttr = normAnswer.contains(normVal);
                    }
                    return new ResultAttribute(
                            new Attribute.Builder()
                                    .withId(pa.getAttribute().getId())
                                    .withName(pa.getAttribute().getName())
                                    .build(),
                            pa.getValue(),
                            isCorrectAttr,
                            isTarget);
                })
                .toList();

        if (logger.isDebugEnabled()) {
            logger.debug("resultAttrs={}", resultAttrs);
        }

        // 3) Enregistrer le résultat (Knowledge + XP)
        recordSingleResult(user, gameMode.getId(), personId, isCorrect, helpUsed);

        // 4) Retourner le verdict + bonne réponse
        return new AnswerValidationResult.Builder()
                .withCorrect(isCorrect)
                .withCorrectAnswer(correctAnswer)
                .withResultAttributes(resultAttrs)
                .build();
    }

    /**
     * VERSION PERF:
     * - 1 seul upsertKnowledge(...) par (gameModeId, personId)
     * - XP events "accumulés" puis crédités après upsert (1 event batch +
     * milestones)
     */
    @Transactional
    public int recordBatchResults(User user, List<KnowledgeResultEvent> events) {
        if (user == null || user.getId() == null)
            return 0;
        if (events == null || events.isEmpty())
            return 0;

        var grouped = events.stream()
                .collect(Collectors.groupingBy(ev -> new GroupKey(ev.gameModeId(), ev.personId())));

        for (var entry : grouped.entrySet()) {
            GroupKey key = entry.getKey();
            List<KnowledgeResultEvent> groupEvents = entry.getValue();

            Knowledge knowledge = knowledgeDao
                    .findByUserGameModeAndPerson(user.getId(), key.gameModeId, key.personId)
                    .orElseGet(() -> new Knowledge.Builder()
                            .withUser(user)
                            .withGameMode(new GameMode.Builder().withId(key.gameModeId).build())
                            .withPerson(new Person.Builder().withId(key.personId).build())
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

            long totalAnswerXp = 0;
            List<StreakMilestoneHit> streakHits = new ArrayList<>();

            int prevGlobalStreak = knowledge.getGlobalStreak();

            // On mémorise aussi si le batch contient au moins une "réponse comptée"
            // (correct+help -> on ne compte pas total_answers)
            boolean hasAnyCountedAnswer = false;

            for (KnowledgeResultEvent ev : groupEvents) {
                applyResultToKnowledge(knowledge, user, ev.correct(), ev.helpUsed());

                int xp = leaderboardService.computeXpForKnowledgeResult(ev.correct(), ev.helpUsed());
                totalAnswerXp += xp;

                // Une "answer" est comptée si NOT(correct && helpUsed)
                if (!(ev.correct() && ev.helpUsed())) {
                    hasAnyCountedAnswer = true;
                }

                int newGlobalStreak = knowledge.getGlobalStreak();
                if (newGlobalStreak != prevGlobalStreak) {
                    int bonus = leaderboardService.computeStreakBonus(newGlobalStreak);
                    if (bonus > 0)
                        streakHits.add(new StreakMilestoneHit(newGlobalStreak, bonus));
                    prevGlobalStreak = newGlobalStreak;
                }
            }

            // Upsert knowledge une fois
            knowledgeDao.upsertKnowledge(knowledge);

            // Créditer XP
            LocalDateTime at = LocalDateTime.now();

            if (totalAnswerXp > 0) {
                leaderboardService.addXp(
                        user,
                        "KNOWLEDGE_ANSWER_BATCH",
                        SOURCE_TYPE_KNOWLEDGE,
                        key.personId,
                        safeLongToInt(totalAnswerXp),
                        at,
                        // pour les compteurs : on met "correct/helpUsed" du batch ?
                        // On choisit: countedAnswer=true -> total_answers+1, sinon +0.
                        // correctAnswersDelta ne peut pas être exact si batch mixte, donc on le laisse
                        // à 0 ici.
                        // (si tu veux exact, il faut passer des deltas et faire une méthode dédiée)
                        false,
                        !hasAnyCountedAnswer // hack pour forcer total_answersDelta=0 dans DAO si "correct&&help"
                );
            }

            // milestones: rares -> ok de faire plusieurs events
            for (StreakMilestoneHit hit : streakHits) {
                leaderboardService.addXp(
                        user,
                        computeStreakEventKey(hit.milestone()),
                        SOURCE_TYPE_KNOWLEDGE,
                        key.personId,
                        hit.bonusXp(),
                        at,
                        false,
                        true // milestones ne doivent pas compter comme answers
                );
            }
        }

        return grouped.size();
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
                    if (!isCorrect || srsDue)
                        scheduler.schedule(k, grade);
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

    private record GroupKey(Long gameModeId, Long personId) {
    }

    // ----------------- POOLS -----------------

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

    public long countDueNow(Course course) {
        return knowledgeDao.countDueNow(course);
    }

    public List<Knowledge> findAllByCourse(Course course) {
        return knowledgeDao.findAllByCourse(course);
    }

    public int resetForCourseScope(long userId, long gameModeId, PopulationScope popScope,
            double baselineEase, double baselineDiff, double baselineStability) {
        return knowledgeDao.resetForCourseScope(userId, gameModeId, popScope, baselineEase, baselineDiff,
                baselineStability);
    }

    public long countToResetForCourseScope(long userId, long gameModeId, PopulationScope popScope) {
        return knowledgeDao.countToResetForCourseScope(userId, gameModeId, popScope);
    }
}
