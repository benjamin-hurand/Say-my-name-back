package com.saymyname.service.course;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.exception.course.CourseAlreadyExistsException;
import com.saymyname.core.exception.course.NextQuestionUnavailableException;
import com.saymyname.core.exception.course.NoMoreQuestionsException;
import com.saymyname.core.exception.course.QuestionAlreadyAnsweredException;
import com.saymyname.core.model.course.AnswerAndNextQuestion;
import com.saymyname.core.model.course.AnswerValidationResult;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionHistory;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.PoolType;
import com.saymyname.persistence.dao.course.CourseDao;

@Service
public class CourseService {

    private final CourseDao courseDao;
    private final KnowledgeService knowledgeService;
    private final CourseQuestionHistoryService courseQuestionHistoryService;

    // Poids à ajuster
    private static final double WEIGHT_ERROR = 5;
    private static final double WEIGHT_SRS = 4;
    private static final double WEIGHT_NOT_SO_NEW = 4;
    private static final double WEIGHT_NEW = 6;
    private static final double WEIGHT_REVISION = 0;

    public CourseService(CourseDao courseDao, KnowledgeService knowledgeService,
            CourseQuestionHistoryService courseQuestionHistoryService) {
        this.courseDao = courseDao;
        this.knowledgeService = knowledgeService;
        this.courseQuestionHistoryService = courseQuestionHistoryService;
    }

    public Optional<Course> getCurrentCourse(long userId) {
        return courseDao.getCurrentCourse(userId);
    }

    @Transactional
    public Course createCourse(Course course) {
        if (courseDao.getCurrentCourse(course.getUser().getId()).isPresent()) {
            // on lance notre exception métier
            throw new CourseAlreadyExistsException();
        }
        Course created = courseDao.saveCourse(course);
        // on lance l'upsert des connaissances
        knowledgeService.insertBatchOfTenKnowledges(created);
        return created;
    }

    @Transactional
    public void updateCourse(Course course) {
        courseDao.saveCourse(course);
    }

    public Course findById(Long courseId) {
        return courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    }

    /**
     * Valide une réponse à une question et renvoie la question suivante
     */
    public AnswerAndNextQuestion answer(Course course,
            CourseQuestionHistory answerHistory) {

        // 1) Incrémenter le cours:
        course.setCurrentRound(course.getCurrentRound() + 1);
        updateCourse(course);

        // 2) Charger l’historique complet en base
        CourseQuestionHistory previous = courseQuestionHistoryService.findById(answerHistory.getId());
        Knowledge knowledge = previous.getKnowledge();

        // 2a) Si déjà répondu, on bloque
        if (previous.getAnsweredAt() != null) {
            throw new QuestionAlreadyAnsweredException(previous.getId());
        }

        // 3) Compléter la réponse
        LocalDateTime now = LocalDateTime.now();
        long delta = ChronoUnit.MILLIS.between(previous.getAskedAt(), now);
        previous.setAnsweredAt(now);
        previous.setResponseTimeMs((int) delta);
        previous.setUserAnswer(answerHistory.getUserAnswer());
        courseQuestionHistoryService.update(previous);

        // 4) Valider la réponse
        AnswerValidationResult validation = knowledgeService.validateAnswer(
                knowledge.getPerson().getId(),
                previous.getUserAnswer(),
                course.getUser(),
                course.getGameMode(),
                previous.isHelpUsed());

        // 5) Enregistrer le booléen “correct”
        previous.setCorrect(validation.isCorrect());

        courseQuestionHistoryService.update(previous);

        // 6) Construire et persister la prochaine question
        CourseQuestionHistory next = findNextDue(
                course, knowledge.getPerson().getId(),
                validation.isCorrect(),
                getFeedbackMessage(validation.isCorrect()), false);
        if (next == null) {
            throw new NextQuestionUnavailableException();
        }
        CourseQuestionHistory savedNext = courseQuestionHistoryService.create(next);

        // 7) Retourner le résultat DTO
        return new AnswerAndNextQuestion.Builder()
                .withIsCorrect(validation.isCorrect())
                .withUserAnswer(previous.getUserAnswer())
                .withCorrectAnswer(validation.getCorrectAnswer())
                .withFeedbackMessage(getFeedbackMessage(validation.isCorrect()))
                .withNextQuestion(savedNext)
                .withResultAttributes(validation.getResultAttributes())
                .build();
    }

    public CourseQuestionHistory continueCourse(Long courseId) {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        if (knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.UNKNOWN) == 0) {
            knowledgeService.insertBatchOfTenKnowledges(course);
        }
        return courseQuestionHistoryService.create(findNextDue(course, null, null, null, true));
    }

    /**
     * Construis le CourseQuestionHistory complet via un tirage pondéré sur les
     * pools
     */
    @Transactional
    private CourseQuestionHistory findNextDue(
            Course course,
            Long lastPersonId,
            Boolean correct,
            String feedback,
            boolean allowRepeat) {

        long userId = course.getUser().getId();
        long gameModeId = course.getGameMode().getId();

        // 1) Les pools et leurs poids
        Map<PoolType, Double> weights = new LinkedHashMap<>(Map.of(
                PoolType.ERROR_RECENT, WEIGHT_ERROR,
                PoolType.SRS_DUE, WEIGHT_SRS,
                PoolType.NEW, WEIGHT_NEW,
                PoolType.DISCOVERED, WEIGHT_NOT_SO_NEW,
                PoolType.REVISION, WEIGHT_REVISION));

        Random rnd = new Random();
        while (!weights.isEmpty()) {
            double sum = weights.values().stream().mapToDouble(Double::doubleValue).sum();
            double r = rnd.nextDouble() * sum;

            PoolType selected = null;
            for (var e : weights.entrySet()) {
                r -= e.getValue();
                if (r <= 0) {
                    selected = e.getKey();
                    break;
                }
            }
            if (selected == null) {
                selected = weights.keySet().iterator().next();
            }

            Knowledge k;
            switch (selected) {
                case ERROR_RECENT ->
                    k = knowledgeService.findFirstRecentError(userId, gameModeId, lastPersonId, allowRepeat);
                case SRS_DUE -> k = knowledgeService.findFirstSRS(userId, gameModeId, lastPersonId, allowRepeat);
                case DISCOVERED ->
                    k = knowledgeService.findFirstDiscovered(userId, gameModeId, lastPersonId, allowRepeat);
                case NEW ->
                    k = knowledgeService.findFirstNew(userId, gameModeId, lastPersonId, allowRepeat);
                default -> k = null;
            }

            // Trouvé
            if (k != null) {
                // On a trouvé, on construit le historique et on retourne
                return new CourseQuestionHistory.Builder()
                        .withCourse(course)
                        .withKnowledge(k)
                        .withQuestionRound(course.getCurrentRound() + 1)
                        .withAskedAt(LocalDateTime.now())
                        .withResponseTimeMs(0) // à remplir plus tard
                        .withUserAnswer(null) // idem
                        .withCorrect(correct != null && correct)
                        .withPoolType(selected)
                        .withHelpUsed(false)
                        .build();
            }

            // Pas trouvé dans ce pool → on enlève le pool et on recommence
            weights.remove(selected);
        }
        // ICI ON A RIEN TROUVE DANS TOUT NOS POOLS
        if (allowRepeat) {
            // on avait déjà autorisé la répétition sans restreindre le lastPersonId, et
            // toujours rien :
            // => cours terminé, on remonte null ou on jette une exception métier
            throw new NoMoreQuestionsException(course.getId());
        } else {
            // on réessaie, cette fois en autorisant la répétition (on vérifie pour
            // lastPersonId)
            return findNextDue(course, lastPersonId, correct, feedback, true);
        }
    }

    private String getFeedbackMessage(Boolean correct) {
        if (correct) {
            return "Correct !";
        } else {
            return "Incorrect !";
        }
    }

}
