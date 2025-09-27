package com.saymyname.service.course;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.exception.course.CourseAlreadyExistsException;
import com.saymyname.core.exception.course.NextQuestionUnavailableException;
import com.saymyname.core.exception.course.NoMoreQuestionsException;
import com.saymyname.core.exception.course.QuestionAlreadyAnsweredException;
import com.saymyname.core.model.course.AnswerAndNextQuestion;
import com.saymyname.core.model.course.AnswerValidationResult;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionHistory;
import com.saymyname.core.model.course.CourseStats;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.CourseStatus;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.PoolType;
import com.saymyname.persistence.dao.course.CourseDao;
import com.saymyname.service.PersonService;
import com.saymyname.service.UserSubscriptionService;

@Service
public class CourseService {

    private final CourseDao courseDao;
    private final KnowledgeService knowledgeService;
    private final CourseQuestionHistoryService courseQuestionHistoryService;
    private final UserSubscriptionService userSubscriptionService;
    private final PersonService personService;

    // Poids à ajuster
    private static final double WEIGHT_ERROR = 5;
    private static final double WEIGHT_SRS = 4;
    private static final double WEIGHT_NOT_SO_NEW = 6;
    private static final double WEIGHT_NEW = 3;
    private static final double WEIGHT_REVISION = 0;

    public CourseService(CourseDao courseDao, KnowledgeService knowledgeService,
            CourseQuestionHistoryService courseQuestionHistoryService, UserSubscriptionService userSubscriptionService,
            PersonService personService) {
        this.courseDao = courseDao;
        this.knowledgeService = knowledgeService;
        this.courseQuestionHistoryService = courseQuestionHistoryService;
        this.userSubscriptionService = userSubscriptionService;
        this.personService = personService;
    }

    public Optional<Course> getCurrentCourse(Long userId) {
        return courseDao.getCurrentCourse(userId);
    }

    @Transactional
    public Course createCourse(Course course) {
        if (courseDao.getCurrentCourse(course.getUser().getId()).isPresent()) {
            throw new CourseAlreadyExistsException();
        }
        Course created = courseDao.saveCourse(course);
        // On lance l'upsert des connaissances (batch initial) selon le scope
        knowledgeService.insertBatchOfTenKnowledges(created);
        return created;
    }

    @Transactional
    public Course restartCourse(Long courseId) {
        Optional<Course> courseOptional = courseDao.findById(courseId);
        Course course = courseOptional
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        // 1) Purger l’historique des questions
        courseQuestionHistoryService.deleteAllByCourse(course);

        // 2) Remettre le statut du cours à IN_PROGRESS
        course.setStatus(CourseStatus.IN_PROGRESS);

        // 3) Sauvegarder & retourner
        return courseDao.saveCourse(course);
    }

    @Transactional
    public Course abandonCourse(Long courseId) {
        Optional<Course> course = courseDao.findById(courseId);
        course.ifPresent(c -> {
            c.setStatus(CourseStatus.ABANDONED);
            courseDao.saveCourse(c);
        });
        return course.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
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
     * Valide une réponse à une question et renvoie la question suivante.
     */
    @Transactional
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
                course,
                knowledge.getPerson().getId(),
                validation.isCorrect(),
                getFeedbackMessage(validation.isCorrect()),
                false);

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

    @Transactional(readOnly = true)
    public CourseStats getStats(Long courseId) {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        // Répartition des knowledges (créés) pour ce user + gameMode
        int unknown = knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.UNKNOWN);
        int discovered = knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.DISCOVERED);
        int learned = knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.LEARNED);
        int mastered = knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.MASTERED);
        int createdTotal = unknown + discovered + learned + mastered;

        // Taille du "scope" (candidats potentiels) + total global persons
        long totalPersonsGlobal = personService.countAll();
        long totalCandidates = switch (course.getPopulationScope()) {
            case FOLLOWED -> userSubscriptionService.countFollowed(course.getUser().getId());
            case ALL -> totalPersonsGlobal;
        };

        // Ratios (safe)
        double createdCoverage = (totalCandidates > 0) ? (double) createdTotal / (double) totalCandidates : 0.0;
        double masteredRatio = (createdTotal > 0) ? (double) mastered / (double) createdTotal : 0.0;

        // Activité
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        int totalAnswers = courseQuestionHistoryService.countAllAnswersByCourse(course);
        int answersToday = courseQuestionHistoryService.countAnswersSince(course, startOfDay);
        LocalDateTime lastActivity = courseQuestionHistoryService.findLastAnsweredAt(course);

        return new CourseStats.Builder()
                .withCourseId(course.getId())
                .withUserId(course.getUser().getId())
                .withGameModeId(course.getGameMode().getId())
                .withPopulationScope(course.getPopulationScope())
                .withTotalCandidates(totalCandidates)
                .withTotalPersonsGlobal(totalPersonsGlobal)
                .withUnknown(unknown)
                .withDiscovered(discovered)
                .withLearned(learned)
                .withMastered(mastered)
                .withCreatedTotal(createdTotal)
                .withCreatedCoverageRatio(createdCoverage)
                .withMasteredRatio(masteredRatio)
                .withTotalAnswers(totalAnswers)
                .withAnswersToday(answersToday)
                .withLastActivity(lastActivity)
                .withCurrentRound(course.getCurrentRound())
                .build();
    }

    /**
     * Construit le CourseQuestionHistory via un tirage pondéré sur les pools.
     */
    @Transactional
    private CourseQuestionHistory findNextDue(
            Course course,
            Long lastPersonId,
            Boolean correct,
            String feedback,
            boolean allowRepeat) {

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
                    k = knowledgeService.findFirstRecentError(course, lastPersonId, allowRepeat);
                case SRS_DUE ->
                    k = knowledgeService.findFirstSRS(course, lastPersonId, allowRepeat);
                case DISCOVERED ->
                    k = knowledgeService.findFirstDiscovered(course, lastPersonId, allowRepeat);
                case NEW ->
                    k = knowledgeService.findFirstNew(course, lastPersonId, allowRepeat);
                default ->
                    k = knowledgeService.findRevision(course, lastPersonId, allowRepeat);
            }

            // Trouvé
            if (k != null) {
                // On a trouvé, on construit l’historique et on retourne
                return new CourseQuestionHistory.Builder()
                        .withCourse(course)
                        .withKnowledge(k)
                        .withQuestionRound(course.getCurrentRound() + 1)
                        .withAskedAt(LocalDateTime.now())
                        .withResponseTimeMs(0)
                        .withUserAnswer(null)
                        .withCorrect(correct != null && correct)
                        .withPoolType(selected)
                        .withHelpUsed(false)
                        .build();
            }

            // Pas trouvé dans ce pool → on enlève le pool et on recommence
            weights.remove(selected);
        }

        // ICI ON A RIEN TROUVÉ DANS TOUS LES POOLS
        if (allowRepeat) {
            // déjà autorisé la répétition → plus rien à proposer
            throw new NoMoreQuestionsException(course.getId());
        } else {
            // on réessaie en autorisant la répétition (évite le blocage si petit scope)
            return findNextDue(course, lastPersonId, correct, feedback, true);
        }
    }

    private String getFeedbackMessage(Boolean correct) {
        return correct ? "Correct !" : "Incorrect !";
    }
}
