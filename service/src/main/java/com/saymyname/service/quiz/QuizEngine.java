// src/main/java/com/saymyname/service/quiz/QuizEngine.java
package com.saymyname.service.quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.course.CourseAnswerResult;
import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.core.model.enums.quiz.QuizPreferredFormat;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.quiz.QuizAnswerResult;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionContext;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import com.saymyname.core.model.quiz.QuizValidationResult;
import com.saymyname.core.model.quiz.options.GameOptions;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.core.model.quiz.snapshot.TruthAttributeValue;
import com.saymyname.core.util.InitialCrafter;
import com.saymyname.persistence.dao.PersonDao;
import com.saymyname.service.GameModeService;
import com.saymyname.service.UserService;
import com.saymyname.service.course.CourseService;

@Service
public class QuizEngine {

        private static final int DEFAULT_TRAINING_LIMIT = 10;
        private static final int MAX_TRAINING_LIMIT = 50;
        private static final int DEFAULT_POOL_SIZE = 30;
        private static final int DEFAULT_TIME_LIMIT_MS = 8000;

        private final PersonDao personDao;
        private final QuizCandidateProvider candidateProvider;
        private final InitialCrafter initialCrafter;

        private final QuizQuestionFactory questionFactory;
        private final QuizAnswerValidator answerValidator;
        private final QuizQuestionSnapshotFactory snapshotFactory;
        private final TrainingQuestionTokenStore tokenStore;

        private final CourseService courseService;
        private final UserService userService;
        private final GameModeService gameModeService;

        public QuizEngine(
                        PersonDao personDao,
                        QuizCandidateProvider candidateProvider,
                        InitialCrafter initialCrafter,
                        QuizQuestionFactory questionFactory,
                        QuizAnswerValidator answerValidator,
                        QuizQuestionSnapshotFactory snapshotFactory,
                        TrainingQuestionTokenStore tokenStore,
                        CourseService courseService,
                        UserService userService,
                        GameModeService gameModeService) {

                this.personDao = Objects.requireNonNull(personDao);
                this.candidateProvider = Objects.requireNonNull(candidateProvider);
                this.initialCrafter = Objects.requireNonNull(initialCrafter);

                this.questionFactory = Objects.requireNonNull(questionFactory);
                this.answerValidator = Objects.requireNonNull(answerValidator);
                this.snapshotFactory = Objects.requireNonNull(snapshotFactory);
                this.tokenStore = Objects.requireNonNull(tokenStore);

                this.courseService = Objects.requireNonNull(courseService);
                this.userService = Objects.requireNonNull(userService);
                this.gameModeService = Objects.requireNonNull(gameModeService);
        }

        // ------------------------------------------------------------------
        // TRAINING — EMIT
        // ------------------------------------------------------------------

        @Transactional(readOnly = true)
        public List<QuizQuestion> emitTraining(
                        GameOptions options,
                        Long userId,
                        QuizPreferredFormat preferredFormat,
                        Boolean requestedTimed,
                        Integer requestedTimeLimitMs,
                        Integer limit) {

                Objects.requireNonNull(options);
                Objects.requireNonNull(options.getGameMode());
                Objects.requireNonNull(userId);

                int size = clampLimit(limit);

                List<Person> persons = personDao.findByOptions(options, userId);
                if (persons.size() > size) {
                        persons = persons.subList(0, size);
                }

                List<Person> pool = candidateProvider.candidates(
                                options, userId, Math.max(DEFAULT_POOL_SIZE, size));

                List<Long> poolIds = pool.stream()
                                .map(Person::getId)
                                .filter(Objects::nonNull)
                                .toList();

                List<Long> targetAttributeIds = options.getGameMode()
                                .getGameModeAttributes().stream()
                                .map(gma -> gma.getAttribute().getId())
                                .toList();

                String operator = options.getGameMode().getOperator();

                List<QuizQuestion> out = new ArrayList<>(persons.size());

                Timing timing = resolveTiming(preferredFormat, requestedTimed, requestedTimeLimitMs);

                for (Person person : persons) {
                        String storageKey = approvedStorageKeyOrThrow(person);
                        String initials = initialCrafter.computeInitials(person, options.getGameMode());

                        QuizQuestionContext ctx = new QuizQuestionContext.Builder()
                                        .withSource(QuizQuestionSource.TRAINING)
                                        .withReducedOptionsId(options.getId())
                                        .build();

                        QuizQuestionSpec spec = new QuizQuestionSpec.Builder()
                                        .withSource(QuizQuestionSource.TRAINING)
                                        .withPersonId(person.getId())
                                        .withStorageKey(storageKey)
                                        .withGameModeId(options.getGameMode().getId())
                                        .withTargetAttributeIds(targetAttributeIds)
                                        .withOperator(operator)
                                        .withContext(ctx)
                                        .withInitials(initials)
                                        .withCandidatePoolPersonIds(poolIds)
                                        .withTimed(timing.timed())
                                        .withTimeLimitMs(timing.timeLimitMs())
                                        .build();

                        // dans emitTraining(...) — boucle for
                        QuizQuestion q = questionFactory.build(spec, preferredFormat);

                        // freeze truth centralisé
                        List<TruthAttributeValue> frozenTruth = snapshotFactory.freezeTruthForQuestion(q);

                        // snapshot sans token
                        QuizQuestionSnapshot snapshot = snapshotFactory.fromQuestion(q, frozenTruth);

                        // token scoped user + snapshot
                        String token = tokenStore.put(userService.getCurrentIdOrThrow(), snapshot, 10 * 60);

                        q.setQuestionToken(token);
                        out.add(q);
                }

                return out;
        }

        // ------------------------------------------------------------------
        // TRAINING — ANSWER
        // ------------------------------------------------------------------

        @Transactional
        public QuizAnswerResult answerTraining(
                        String questionToken,
                        QuizAnswerSubmission submission,
                        boolean helpUsed) {

                if (questionToken == null || questionToken.isBlank()) {
                        throw new IllegalArgumentException("questionToken is required");
                }

                Long userId = userService.getCurrentIdOrThrow();

                TrainingQuestionTokenStore.StoredTrainingQuestion stored = tokenStore.consume(questionToken, userId)
                                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

                QuizQuestionSnapshot snapshot = stored.snapshot();

                QuizValidationResult validation = answerValidator.validateFromSnapshot(snapshot, submission);

                return new QuizAnswerResult.Builder()
                                .withCorrect(validation.isCorrect())
                                .withUserAnswer(submission != null ? submission.getUserAnswer() : null)
                                .withCorrectAnswer(validation.getCorrectAnswerDisplay())
                                .withFeedbackMessage(validation.isCorrect() ? "Correct !" : "Incorrect !")
                                .withResultAttributes(validation.getResultAttributes())
                                .withFollowUp(null)
                                .build();
        }

        // ------------------------------------------------------------------
        // COURSE — DELEGATION (Option A)
        // ------------------------------------------------------------------

        @Transactional
        public QuizQuestion emitCourseNext(
                        Long courseId,
                        QuizPreferredFormat preferredFormat,
                        Boolean timed,
                        Integer timeLimitMs) {
                Objects.requireNonNull(courseId);
                return courseService.continueCourse(courseId, preferredFormat, timed, timeLimitMs);
        }

        @Transactional
        public CourseAnswerResult answerCourse(
                        Long courseId,
                        Long courseQuestionHistoryId,
                        QuizPreferredFormat preferredFormat,
                        Boolean timed,
                        Integer timeLimitMs,
                        QuizAnswerSubmission submission) {

                Objects.requireNonNull(courseId);
                Objects.requireNonNull(courseQuestionHistoryId);

                return courseService.answer(
                                courseService.findById(courseId),
                                courseQuestionHistoryId,
                                preferredFormat,
                                timed,
                                timeLimitMs,
                                submission);
        }

        // ------------------------------------------------------------------
        // helpers
        // ------------------------------------------------------------------

        private static int clampLimit(Integer limit) {
                if (limit == null || limit <= 0)
                        return DEFAULT_TRAINING_LIMIT;
                return Math.min(limit, MAX_TRAINING_LIMIT);
        }

        private Timing resolveTiming(
                        QuizPreferredFormat preferredFormat,
                        Boolean requestedTimed,
                        Integer requestedTimeLimitMs) {
                boolean timed = requestedTimed != null ? requestedTimed.booleanValue()
                                : autoTimedForTraining(preferredFormat);

                Integer timeLimitMs = requestedTimeLimitMs;
                if (timed) {
                        if (timeLimitMs == null || timeLimitMs < 1000) {
                                timeLimitMs = DEFAULT_TIME_LIMIT_MS;
                        }
                } else {
                        timeLimitMs = null;
                }

                return new Timing(timed, timeLimitMs);
        }

        private boolean autoTimedForTraining(QuizPreferredFormat preferredFormat) {
                if (preferredFormat == null || preferredFormat == QuizPreferredFormat.AUTO) {
                        return false;
                }
                return false;
        }

        private record Timing(boolean timed, Integer timeLimitMs) {
        }

        private static String approvedStorageKeyOrThrow(Person person) {
                return person.getPhotos().stream()
                                .filter(p -> p.getStatus() == PhotoStatus.APPROVED)
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException(
                                                "Person " + person.getId() + " has no APPROVED photo"))
                                .getStorageKey();
        }
}
