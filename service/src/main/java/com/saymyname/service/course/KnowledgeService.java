package com.saymyname.service.course;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.core.model.game.options.GameMode;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.core.util.AnswerValidator;
import com.saymyname.persistence.dao.PersonAttributeDao;
import com.saymyname.persistence.dao.course.KnowledgeDao;
import com.saymyname.service.course.scheduler.SchedulerStrategy;

@Service
public class KnowledgeService {

    private final KnowledgeDao knowledgeDao;
    private final Map<SrsAlgorithm, SchedulerStrategy> strategies;
    private final SrsAlgorithm defaultAlgorithm;
    private final PersonAttributeDao personAttributeDao;
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeService.class);

    private static final BigDecimal INITIAL_EF = BigDecimal.valueOf(2.5);
    private static final double INITIAL_DIFF = 1.0;
    private static final double INITIAL_STABILITY = 1.0;

    public KnowledgeService(
            KnowledgeDao knowledgeDao,
            Map<SrsAlgorithm, SchedulerStrategy> strategies,
            @Value("${quiz.reviewAlgorithm:SM2}") SrsAlgorithm defaultAlgorithm,
            PersonAttributeDao personAttributeDao) {
        this.knowledgeDao = knowledgeDao;
        this.strategies = strategies;
        this.defaultAlgorithm = defaultAlgorithm;
        this.personAttributeDao = personAttributeDao;
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
        // on utilise le batch sous le capot
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
                        // normalisation basique, à adapter si vous utilisez un utilitaire
                        String normAnswer = answer.trim().toLowerCase();
                        String normVal = pa.getValue().trim().toLowerCase();
                        if (gameMode.getOperator() == "AND") {
                            isCorrectAttr = normAnswer.contains(normVal);
                        } else {
                            isCorrectAttr = normAnswer.contains(normVal);
                        }
                    }
                    return new ResultAttribute(
                            new Attribute.Builder().withId(pa.getAttribute().getId())
                                    .withName(pa.getAttribute().getName()).build(),
                            pa.getValue(),
                            isCorrectAttr,
                            isTarget);
                })
                .toList();
        logger.info("resultAttrs =" + resultAttrs);

        // 3) Enregistrer le résultat
        recordSingleResult(user, gameMode.getId(), personId, isCorrect, helpUsed);

        // 4) Retourner le verdict + bonne réponse
        return new AnswerValidationResult.Builder()
                .withCorrect(isCorrect)
                .withCorrectAnswer(correctAnswer)
                .withResultAttributes(resultAttrs)
                .build();
    }

    /**
     * Traite un batch d'événements KnowledgeResultEvent pour l'utilisateur donné.
     * Regroupe par (gameModeId, personId), charge ou crée un seul Knowledge par
     * groupe,
     * applique tous les events, et upsert en base.
     *
     * @param user   l'utilisateur authentifié
     * @param events la liste d'événements métier
     * @return le nombre de Knowledge distincts mis à jour
     */
    @Transactional
    public int recordBatchResults(User user, List<KnowledgeResultEvent> events) {
        // 1) Grouper les events par (gameModeId, personId)
        var grouped = events.stream()
                .collect(Collectors.groupingBy(ev -> new GroupKey(ev.gameModeId(), ev.personId())));

        // 2) Pour chaque groupe, loadOrCreate, appliquer puis upsert
        for (var entry : grouped.entrySet()) {
            GroupKey key = entry.getKey();

            Knowledge knowledge = knowledgeDao
                    .findByUserGameModeAndPerson(user.getId(), key.gameModeId, key.personId)
                    .orElseGet(() -> {
                        // création si jamais aucun Knowledge existant
                        return new Knowledge.Builder().withUser(user)
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
                                .withStability(INITIAL_STABILITY).build();
                    });
            logger.info("notre knowledge: " + knowledge);
            // Appliquer chaque résultat sur ce même Knowledge
            for (KnowledgeResultEvent ev : entry.getValue()) {
                applyResultToKnowledge(knowledge, user, ev.correct(), ev.helpUsed());
            }

            // Persister l'état final
            knowledgeDao.upsertKnowledge(knowledge);
        }

        // On retourne le nombre de knowledges mis à jour
        return grouped.size();
    }

    /**
     * Applique un seul résultat (correct/helpUsed) sur un Knowledge donné :
     * – mise à jour des compteurs (total, success, failure, streaks)
     * – scheduling SRS selon l'algorithme de l'utilisateur
     * – mise à jour du status (UNKNOWN → DISCOVERED → LEARNED)
     */
    private void applyResultToKnowledge(
            Knowledge k,
            User user,
            boolean isCorrect,
            boolean helpUsed) {
        LocalDateTime now = LocalDateTime.now();

        // 1) Compteurs et streaks
        if (!(isCorrect && helpUsed)) {
            k.setTotalRepetitionCount(k.getTotalRepetitionCount() + 1);

            if (isCorrect) {
                k.setSuccessCount(k.getSuccessCount() + 1);
                k.setGlobalStreak(Math.max(1, k.getGlobalStreak() + 1));
            } else {
                k.setFailureCount(k.getFailureCount() + 1);
                k.setGlobalStreak(Math.min(-1, k.getGlobalStreak() - 1));
            }

            // 2) Scheduling SRS
            boolean srsDue = k.getNextReviewDate() == null
                    || !k.getNextReviewDate().isAfter(now);
            SrsAlgorithm algo = user.getSrsAlgorithm() != null
                    ? user.getSrsAlgorithm()
                    : defaultAlgorithm;
            SchedulerStrategy scheduler = strategies.get(algo);
            int grade = scheduler.mapGrade(isCorrect);

            switch (algo) {
                case SM2:
                    if (!isCorrect || srsDue) {
                        scheduler.schedule(k, grade);
                    }
                    break;
                case PFA:
                case FSRS:
                    scheduler.schedule(k, grade);
                    break;
            }
        } else {
            // correct + aide → reset des streaks, pas de répétition comptée
            k.setGlobalStreak(0);
            k.setSrsStreak(0);
        }

        // 3) Changement de statut
        KnowledgeStatus status = k.getStatus();
        if (status == KnowledgeStatus.UNKNOWN) {
            status = KnowledgeStatus.DISCOVERED;
        }
        if (isCorrect && !helpUsed && status == KnowledgeStatus.DISCOVERED) {
            status = KnowledgeStatus.LEARNED;
        }
        k.setStatus(status);
    }

    /** Clé de regroupement pour batcher par gameMode + person */
    private record GroupKey(Long gameModeId, Long personId) {
    }

    // POOLS
    // UNKNOWN
    public Knowledge findFirstNew(Long courseId, Long userId, Long gameModeId, Long lastPersonId, boolean allowRepeat) {
        return knowledgeDao.findFirstNew(courseId, userId, gameModeId, lastPersonId, allowRepeat);
    }

    // DISCOVERED
    public Knowledge findFirstDiscovered(Long courseId, Long userId, Long gameModeId, Long lastPersonId,
            boolean allowRepeat) {
        return knowledgeDao.findFirstDiscovered(courseId, userId, gameModeId, lastPersonId, allowRepeat);
    }

    // LEARNED: recent errors
    public Knowledge findFirstRecentError(Long courseId, Long userId, Long gameModeId, Long lastPersonId,
            boolean allowRepeat) {
        return knowledgeDao.findFirstRecentError(courseId, userId, gameModeId, lastPersonId, allowRepeat);
    }

    // LEARNED: srs due
    public Knowledge findFirstSRS(Long courseId, Long userId, Long gameModeId, Long lastPersonId,
            boolean allowRepeat) {
        return knowledgeDao.findFirstSRS(courseId, userId, gameModeId, lastPersonId, allowRepeat);
    }

    // [BONUS REVISION] MASTERED and LEARNED: future srs due
    public Knowledge findRevision(Long courseId, Long userId, Long gameModeId, Long lastPersonId, boolean allowRepeat) {
        return knowledgeDao.findRevision(courseId, userId, gameModeId, lastPersonId, allowRepeat);
    }

    public List<Knowledge> findAllByCourse(Course course) {
        return knowledgeDao.findAllByCourse(course);
    }

}
