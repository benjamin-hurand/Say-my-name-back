package com.saymyname.service.course;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
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
import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.course.AnswerAndNextQuestion;
import com.saymyname.core.model.course.AnswerValidationResult;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionHistory;
import com.saymyname.core.model.course.CourseStats;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.CourseStatus;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.PoolType;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.persistence.dao.course.CourseDao;
import com.saymyname.service.PersonService;
import com.saymyname.service.UserService;
import com.saymyname.service.UserSubscriptionService;

@Service
public class CourseService {

    private final CourseDao courseDao;
    private final KnowledgeService knowledgeService;
    private final CourseQuestionHistoryService courseQuestionHistoryService;
    private final UserSubscriptionService userSubscriptionService;
    private final PersonService personService;
    private final UserService userService;

    // Statuts actifs = uniquement IN_PROGRESS
    private static final List<CourseStatus> ACTIVE_STATUSES = List.of(CourseStatus.IN_PROGRESS);

    // Poids des pools
    private static final double WEIGHT_ERROR = 5;
    private static final double WEIGHT_SRS = 4;
    private static final double WEIGHT_NOT_SO_NEW = 6;
    private static final double WEIGHT_NEW = 3;
    private static final double WEIGHT_REVISION = 0;

    public CourseService(CourseDao courseDao, KnowledgeService knowledgeService,
            CourseQuestionHistoryService courseQuestionHistoryService, UserSubscriptionService userSubscriptionService,
            PersonService personService, UserService userService) {
        this.courseDao = courseDao;
        this.knowledgeService = knowledgeService;
        this.courseQuestionHistoryService = courseQuestionHistoryService;
        this.userSubscriptionService = userSubscriptionService;
        this.personService = personService;
        this.userService = userService;
    }

    /** Dernier cours “focus” (lastAccessedAt) parmi les actifs, sinon fallback. */
    @Transactional(readOnly = true)
    public Optional<Course> getLastUsedCourse() {
        Long userId = userService.getCurrentIdOrThrow();

        // 1) Dernier cours “focus/last accessed” parmi les actifs
        Optional<Course> focused = courseDao.findLastAccessedFirstActive(userId, ACTIVE_STATUSES);
        if (focused.isPresent())
            return focused;

        // 2) Sinon, premier actif par “updatedAt”
        var actives = courseDao.findAllByUserAndStatusesOrderedByUpdatedAt(userId, ACTIVE_STATUSES);
        if (!actives.isEmpty())
            return Optional.of(actives.get(0));

        // 3) Sinon, éventuellement le “current course” (si ta notion diffère)
        return courseDao.getCurrentCourse(userId);
    }

    @Transactional
    public Course createCourse(Course proto) {
        // 🔐 Attache l’utilisateur courant (dérivé du JWT)
        User me = userService.getCurrentAuthenticatedUserOrThrow();
        proto.setUser(me);

        // (Optionnel) garde-fous si jamais le mapper ne l’a pas déjà fait
        if (proto.getPopulationScope() == null) {
            proto.setPopulationScope(PopulationScope.FOLLOWED);
        }
        if (proto.getStatus() == null) {
            proto.setStatus(CourseStatus.IN_PROGRESS);
        }

        // Unicité : (user, mode, scope) en IN_PROGRESS
        var existing = courseDao.findFirstByUserModeScopeAndStatus(
                me.getId(),
                proto.getGameMode().getId(),
                proto.getPopulationScope(),
                CourseStatus.IN_PROGRESS);

        if (existing.isPresent()) {
            throw new CourseAlreadyExistsException();
        }

        Course created = courseDao.saveCourse(proto);
        knowledgeService.insertBatchOfTenKnowledges(created);
        return created;
    }

    /**
     * Crée si aucun IN_PROGRESS pour (user,mode,scope), sinon renvoie l’existant.
     */
    @Transactional
    public Course createOrResume(Course proto) {
        // 🔐 Attache l’utilisateur courant (dérivé du JWT)
        User me = userService.getCurrentAuthenticatedUserOrThrow();
        proto.setUser(me);

        if (proto.getPopulationScope() == null) {
            proto.setPopulationScope(PopulationScope.FOLLOWED);
        }
        if (proto.getStatus() == null) {
            proto.setStatus(CourseStatus.IN_PROGRESS);
        }

        var existing = courseDao.findFirstByUserModeScopeAndStatus(
                me.getId(),
                proto.getGameMode().getId(),
                proto.getPopulationScope(),
                CourseStatus.IN_PROGRESS);

        if (existing.isPresent()) {
            return existing.get();
        }

        Course created = courseDao.saveCourse(proto);
        knowledgeService.insertBatchOfTenKnowledges(created);
        return created;
    }

    @Transactional
    public Course restartCourse(Long courseId, long userId) {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        if (!course.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        // 1) purge historique local
        courseQuestionHistoryService.deleteAllByCourse(course);

        // 2) reset “dur” des knowledges pour le périmètre du cours
        final double BASELINE_EASE = 2.5;
        final double BASELINE_DIFF = 1.0;
        final double BASELINE_STAB = 1.0;
        var scope = course.getPopulationScope() != null ? course.getPopulationScope() : PopulationScope.FOLLOWED;

        knowledgeService.resetForCourseScope(
                userId,
                course.getGameMode().getId(),
                scope,
                BASELINE_EASE, BASELINE_DIFF, BASELINE_STAB);

        // 3) méta du course
        course.setStatus(CourseStatus.IN_PROGRESS);
        course.setCurrentRound(0);

        return courseDao.saveCourse(course);
    }

    @Transactional
    public void updateCourse(Course course) {
        courseDao.saveCourse(course);
    }

    public Course findById(Long courseId) {
        return courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    }

    /** Marque le “focus” utilisateur sur ce cours. */
    @Transactional
    public void touchLastAccessed(Long courseId) {
        courseDao.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        courseDao.touchLastAccessed(courseId, LocalDateTime.now());
    }

    /** Démarrer/récupérer la prochaine question (marque aussi le focus). */
    @Transactional
    public CourseQuestionHistory continueCourse(Long courseId) {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        courseDao.touchLastAccessed(course.getId(), LocalDateTime.now());

        if (knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.UNKNOWN) == 0) {
            knowledgeService.insertBatchOfTenKnowledges(course);
        }
        return courseQuestionHistoryService.create(findNextDue(course, null, null, null, true));
    }

    @Transactional(readOnly = true)
    public CourseStats getStats(Long courseId) {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        long unknown = knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.UNKNOWN);
        long discovered = knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.DISCOVERED);
        long learned = knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.LEARNED);
        long mastered = knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.MASTERED);

        long totalCandidates = userSubscriptionService.countFollowedEligibleForMode(course);
        long universeEligible = personService.countUniverseEligibleForMode(course);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long totalAnswers = courseQuestionHistoryService.countAllAnswersByCourse(course);
        long answersToday = courseQuestionHistoryService.countAnswersSince(course, startOfDay);
        LocalDateTime lastActivity = courseQuestionHistoryService.findLastAnsweredAt(course);

        long dueNow = knowledgeService.countDueNow(course);

        return new CourseStats.Builder()
                .withCourseId(course.getId())
                .withGameModeId(course.getGameMode().getId())
                .withTotalCandidates(totalCandidates)
                .withUniverseEligible(universeEligible)
                .withUnknown(unknown)
                .withDiscovered(discovered)
                .withLearned(learned)
                .withMastered(mastered)
                .withTotalAnswers(totalAnswers)
                .withAnswersToday(answersToday)
                .withLastActivity(lastActivity)
                .withCurrentRound(course.getCurrentRound())
                .withDueNow(dueNow)
                .build();
    }

    @Transactional(readOnly = true)
    public List<Course> findAllByUser() {
        Long userId = userService.getCurrentIdOrThrow();

        // 1) Triés par “last access” si dispo
        var list = courseDao.findAllByUserAndStatusesOrderedByLastAccess(userId, ACTIVE_STATUSES);
        if (!list.isEmpty())
            return list;

        // 2) Sinon, fallback sur “updatedAt”
        return courseDao.findAllByUserAndStatusesOrderedByUpdatedAt(userId, ACTIVE_STATUSES);
    }

    @Transactional(readOnly = true)
    public List<CourseStats> getStatsForUser() {
        return findAllByUser().stream()
                .map(c -> getStats(c.getId()))
                .toList();
    }

    // ------ pools ------
    @Transactional
    private CourseQuestionHistory findNextDue(
            Course course,
            Long lastPersonId,
            Boolean correct,
            String feedback,
            boolean allowRepeat) {

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
            if (selected == null)
                selected = weights.keySet().iterator().next();

            Knowledge k;
            switch (selected) {
                case ERROR_RECENT -> k = knowledgeService.findFirstRecentError(course, lastPersonId, allowRepeat);
                case SRS_DUE -> k = knowledgeService.findFirstSRS(course, lastPersonId, allowRepeat);
                case DISCOVERED -> k = knowledgeService.findFirstDiscovered(course, lastPersonId, allowRepeat);
                case NEW -> k = knowledgeService.findFirstNew(course, lastPersonId, allowRepeat);
                default -> k = knowledgeService.findRevision(course, lastPersonId, allowRepeat);
            }

            if (k != null) {
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
            weights.remove(selected);
        }

        if (allowRepeat) {
            throw new NoMoreQuestionsException(course.getId());
        } else {
            return findNextDue(course, lastPersonId, correct, feedback, true);
        }
    }

    private String getFeedbackMessage(Boolean correct) {
        return correct ? "Correct !" : "Incorrect !";
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
}
