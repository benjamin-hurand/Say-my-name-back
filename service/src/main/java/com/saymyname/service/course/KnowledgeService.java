package com.saymyname.service.course;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.common.User;
import com.saymyname.core.model.course.AnswerValidationResult;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.course.ResultAttribute;
import com.saymyname.persistence.dao.PersonAttributeDao;
import com.saymyname.persistence.dao.course.KnowledgeDao;
import com.saymyname.service.ChallengeService;
import com.saymyname.service.course.scheduler.SchedulerStrategy;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.ReviewAlgorithm;
import com.saymyname.core.model.game.QuizEntry;
import com.saymyname.core.model.game.options.GameMode;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.core.util.AnswerValidator;

@Service
public class KnowledgeService {

    private final KnowledgeDao knowledgeDao;
    private final Map<ReviewAlgorithm, SchedulerStrategy> strategies;
    private final ReviewAlgorithm defaultAlgorithm;
    private final PersonAttributeDao personAttributeDao;
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeService.class);

    public KnowledgeService(
            KnowledgeDao knowledgeDao,
            Map<ReviewAlgorithm, SchedulerStrategy> strategies,
            @Value("${quiz.reviewAlgorithm:SM2}") ReviewAlgorithm defaultAlgorithm,
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

    @Transactional
    public AnswerValidationResult validateAnswer(
            Long personId,
            String answer,
            User user,
            GameMode gameMode,
            boolean helpUsed) {

        // 1) Charger la connaissance
        Knowledge k = knowledgeDao.findByUserGameModeAndPerson(
                user.getId(),
                gameMode.getId(),
                personId);

        // 2) Calculer le verdict
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

        // 3) Construire la liste des ResultAttribute
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

        if (!(isCorrect && helpUsed)) {
            // 3) Mises à jour globales (UX/UI + PFA)
            LocalDateTime now = LocalDateTime.now();
            k.setTotalRepetitionCount(k.getTotalRepetitionCount() + 1);

            int globalStreak = k.getGlobalStreak();
            if (isCorrect) {
                k.setSuccessCount(k.getSuccessCount() + 1);
                globalStreak = (globalStreak < 0) ? 1 : globalStreak + 1;
            } else {
                k.setFailureCount(k.getFailureCount() + 1);
                globalStreak = (globalStreak > 0) ? -1 : globalStreak - 1;
            }
            k.setGlobalStreak(globalStreak);

            // 4) Détecter si c’est une révision SRS due
            boolean isSrsDue = !k.getNextReviewDate().isAfter(now);

            // 5) Scheduler selon l’algo choisi (skip si helpUsed+correct)
            ReviewAlgorithm algo = user.getSrsAlgorithm() != null
                    ? user.getSrsAlgorithm()
                    : defaultAlgorithm;
            SchedulerStrategy scheduler = strategies.get(algo);
            int grade = scheduler.mapGrade(isCorrect);

            switch (algo) {
                case SM2:
                    // SM-2 : only on failure or on scheduled review
                    if (!isCorrect || isSrsDue) {
                        scheduler.schedule(k, grade);
                    }
                    break;

                case PFA:
                    // PFA : every repetition
                    scheduler.schedule(k, grade);
                    break;

                case FSRS:
                    // FSRS : every repetition
                    scheduler.schedule(k, grade);
                    break;
            }

        } else {
            k.setGlobalStreak(0);
            k.setSrsStreak(0);
        }

        // 6) Changement de statut
        KnowledgeStatus status = k.getStatus();

        // 6a) Si UNKNOWN, on passe DISCOVERED que l'aide aie été utilisée ou non.
        if (status == KnowledgeStatus.UNKNOWN)
            status = KnowledgeStatus.DISCOVERED;

        // 6b) Si réponse correcte sans aide et status DISCOVERED on passe LEARNED
        if (isCorrect && !helpUsed && status == KnowledgeStatus.DISCOVERED) {
            status = KnowledgeStatus.LEARNED;
        }
        k.setStatus(status);

        // 7) Persister la connaissance mise à jour
        knowledgeDao.upsertKnowledge(k);

        // 8) Retourner le verdict + bonne réponse
        return new AnswerValidationResult.Builder()
                .withCorrect(isCorrect)
                .withCorrectAnswer(correctAnswer)
                .withResultAttributes(resultAttrs)
                .build();
    }

    // POOLS
    // UNKNOWN
    public Knowledge findFirstNew(long userId, long gameModeId, Long lastPersonId, boolean allowRepeat) {
        return knowledgeDao.findFirstNew(userId, gameModeId, lastPersonId, allowRepeat);
    }

    // DISCOVERED
    public Knowledge findFirstDiscovered(long userId, long gameModeId, Long lastPersonId, boolean allowRepeat) {
        return knowledgeDao.findFirstDiscovered(userId, gameModeId, lastPersonId, allowRepeat);
    }

    // LEARNED: recent errors
    public Knowledge findFirstRecentError(long userId, long gameModeId, Long lastPersonId, boolean allowRepeat) {
        return knowledgeDao.findFirstRecentError(userId, gameModeId, lastPersonId, allowRepeat);
    }

    // LEARNED: srs due
    public Knowledge findFirstSRS(long userId, long gameModeId, Long lastPersonId,
            boolean allowRepeat) {
        return knowledgeDao.findFirstSRS(userId, gameModeId, lastPersonId, allowRepeat);
    }

    // [BONUS REVISION] MASTERED and LEARNED: future srs due
    public Knowledge findRevision(long userId, long gameModeId, Long lastPersonId, boolean allowRepeat) {
        return knowledgeDao.findRevision(userId, gameModeId, lastPersonId, allowRepeat);
    }

    public List<Knowledge> findAllByCourse(Course course) {
        return knowledgeDao.findAllByCourse(course);
    }
}
