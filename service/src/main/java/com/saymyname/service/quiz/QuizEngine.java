// src/main/java/com/saymyname/service/quiz/QuizEngine.java
package com.saymyname.service.quiz;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.course.KnowledgeResultEvent;
import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.core.model.enums.course.QuizQuestionItemRole;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPreferredFormat;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.quiz.QuizAnswerItemResult;
import com.saymyname.core.model.quiz.QuizAnswerResult;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizEvaluationResult;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionContext;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import com.saymyname.core.model.quiz.QuizPayloadItem;
import com.saymyname.core.model.quiz.QuizValidationResult;
import com.saymyname.core.model.quiz.options.GameOptions;
import com.saymyname.core.model.quiz.planning.PlanningContext;
import com.saymyname.core.model.quiz.planning.PlanningRequest;
import com.saymyname.core.model.quiz.planning.QuestionPlan;
import com.saymyname.core.model.quiz.snapshot.HangmanSnapshotState;
import com.saymyname.core.model.quiz.snapshot.MultiStepState;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.core.model.quiz.snapshot.TruthAttributeValue;
import com.saymyname.core.model.quiz.snapshot.WordPuzzleSnapshotState;
import com.saymyname.core.util.InitialCrafter;
import com.saymyname.persistence.dao.PersonDao;
import com.saymyname.service.UserService;
import com.saymyname.service.course.KnowledgeService;
import com.saymyname.service.course.KnowledgeStatsService;
import com.saymyname.service.quiz.handle.AttemptRef;
import com.saymyname.service.quiz.handle.QuizHandleCodec;
import com.saymyname.service.quiz.planning.QuestionPlanningService;
import com.saymyname.service.quiz.store.QuizAttemptStore;
import com.saymyname.service.quiz.store.QuizAttemptStore.AttemptHandle;

@Service
public class QuizEngine {

        private static final int DEFAULT_POOL_SIZE = 30;
        private static final long DEFAULT_TRAINING_TTL_SEC = 10 * 60;

        private static final Logger log = LoggerFactory.getLogger(QuizEngine.class);

        @SuppressWarnings("unused")
        private final PersonDao personDao; // kept because it is injected today; may be removable later
        private final QuizCandidateProvider candidateProvider;
        private final InitialCrafter initialCrafter;

        private final QuizQuestionFactory questionFactory;
        private final QuizAnswerValidator answerValidator;
        private final QuizQuestionSnapshotFactory snapshotFactory;

        private final QuizAttemptStore attemptStore;

        private final KnowledgeService knowledgeService;
        private final KnowledgeStatsService knowledgeStatsService;
        private final QuestionPlanningService questionPlanningService;

        private final UserService userService;
        private final QuizHandleCodec quizHandleCodec;

        public QuizEngine(
                        PersonDao personDao,
                        QuizCandidateProvider candidateProvider,
                        InitialCrafter initialCrafter,
                        QuizQuestionFactory questionFactory,
                        QuizAnswerValidator answerValidator,
                        QuizQuestionSnapshotFactory snapshotFactory,
                        QuizAttemptStore attemptStore,
                        KnowledgeService knowledgeService,
                        KnowledgeStatsService knowledgeStatsService,
                        QuestionPlanningService questionPlanningService,
                        UserService userService,
                        QuizHandleCodec quizHandleCodec) {

                this.personDao = Objects.requireNonNull(personDao);
                this.candidateProvider = Objects.requireNonNull(candidateProvider);
                this.initialCrafter = Objects.requireNonNull(initialCrafter);

                this.questionFactory = Objects.requireNonNull(questionFactory);
                this.answerValidator = Objects.requireNonNull(answerValidator);
                this.snapshotFactory = Objects.requireNonNull(snapshotFactory);
                this.attemptStore = Objects.requireNonNull(attemptStore);

                this.knowledgeService = Objects.requireNonNull(knowledgeService);
                this.knowledgeStatsService = Objects.requireNonNull(knowledgeStatsService);
                this.questionPlanningService = Objects.requireNonNull(questionPlanningService);

                this.userService = Objects.requireNonNull(userService);
                this.quizHandleCodec = Objects.requireNonNull(quizHandleCodec);
        }

        // ------------------------------------------------------------------
        // TRAINING/REVIEW entrypoints (QuizEngine handles only token-based flows)
        // Note: COURSE flow is handled directly by CourseService
        // ------------------------------------------------------------------

        /**
         * Answer a training/review question.
         * COURSE questions should be answered via CourseService.answer() directly.
         */
        @Transactional
        public QuizAnswerResult answerTraining(String questionHandle, QuizAnswerSubmission submission) {
                AttemptRef ref = quizHandleCodec.decodeOrThrow(questionHandle);

                if (ref.source() == QuizQuestionSource.COURSE) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "COURSE questions should be answered via CourseService.answer()");
                }

                return answerTokenBased(
                                ref.source(),
                                ref.handle(),
                                userService.getCurrentIdOrThrow(),
                                submission,
                                false);
        }

        // ------------------------------------------------------------------
        // TRAINING - ANSWER WITH NEXT (atomic answer + emit next)
        // ------------------------------------------------------------------

        /**
         * Answer training question + optionally emit next question atomically.
         * Called by QuizRestController when nextRequest is provided.
         */
        @Transactional
        public QuizAnswerResult answerTrainingWithNext(
                        String questionHandle,
                        QuizAnswerSubmission submission,
                        GameOptions nextOptions,
                        QuizPreferredFormat nextPreferred,
                        Boolean nextTimed,
                        Integer nextTimeLimitMs) {

                QuizAnswerResult result = answerTraining(questionHandle, submission);

                // If complete and options are provided, emit the next question
                if (Boolean.TRUE.equals(result.getIsComplete()) && nextOptions != null) {
                        Long userId = userService.getCurrentIdOrThrow();
                        QuizQuestion next = emitTraining(nextOptions, userId, nextPreferred, nextTimed,
                                        nextTimeLimitMs);
                        return new QuizAnswerResult.Builder()
                                        .from(result)
                                        .withNextQuestion(next)
                                        .build();
                }
                return result;
        }

        // ------------------------------------------------------------------
        // TRAINING/REVIEW - EMIT
        // ------------------------------------------------------------------

        @Transactional(readOnly = true)
        public QuizQuestion emitTraining(
                        GameOptions options,
                        Long userId,
                        QuizPreferredFormat preferredFormat,
                        Boolean requestedTimed,
                        Integer requestedTimeLimitMs) {

                Objects.requireNonNull(options, "options");
                Objects.requireNonNull(options.getGameMode(), "options.gameMode");
                Objects.requireNonNull(userId, "userId");

                // 1) Pick a candidate from the pool driven by the user's options.
                // Product behavior: if none available, user should change options.
                List<Person> pool = candidateProvider.candidates(options, userId, DEFAULT_POOL_SIZE);
                if (pool == null) {
                        pool = List.of();
                }

                Person person = selectTrainingCandidate(pool, null);
                if (person == null) {
                        log.warn("No eligible candidate person found for training optionsId={}, userId={}",
                                        options.getId(), userId);
                        throw new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "No candidate person available for training (adjust your options)");
                }

                // 2) Load Knowledge anchor (optional in training, but used by
                // planning/anti-repetition).
                Long gameModeId = options.getGameMode().getId();
                Knowledge knowledge = knowledgeService.findByUserGameModeAndPerson(userId, gameModeId, person.getId());

                // 3) Build planning request (training context, multi-target disabled).
                boolean trackKnowledge = Boolean.TRUE.equals(options.isTrackKnowledge());
                PlanningContext planningContext = PlanningContext.forTraining(trackKnowledge);

                PlanningRequest planningRequest = PlanningRequest.builder()
                                .userId(userId)
                                .gameModeId(gameModeId)
                                .populationScope(options.getPopulationScope())
                                .gameOptions(options)
                                .context(planningContext)
                                .primaryKnowledge(knowledge)
                                .preferredFormat(preferredFormat)
                                .requestedTimed(requestedTimed)
                                .requestedTimeLimitMs(requestedTimeLimitMs)
                                .build();

                // 4) Plan (format + candidates + timing + distractors).
                QuestionPlan plan = questionPlanningService.plan(planningRequest);

                // 5) In training, the planner may return a primaryCandidate; keep it if valid,
                // else fallback.
                Person planned = plan.primaryCandidate();
                Person plannedCandidate = selectTrainingCandidate(
                                planned == null ? List.of() : List.of(planned),
                                person.getId());

                if (plannedCandidate == null) {
                        log.warn("Question planning returned no eligible primary candidate; fallback to initial person userId={}",
                                        userId);
                        plannedCandidate = person;
                }

                // If planner changed target, refresh knowledge anchor.
                if (!plannedCandidate.getId().equals(person.getId())) {
                        person = plannedCandidate;
                        knowledge = knowledgeService.findByUserGameModeAndPerson(userId, gameModeId, person.getId());
                }

                // 6) Build question spec.
                List<Long> targetAttributeIds = options.getGameMode()
                                .getGameModeAttributes().stream()
                                .map(gma -> gma.getAttribute().getId())
                                .toList();

                String operator = options.getGameMode().getOperator();

                String storageKey = approvedStorageKeyOrThrow(person);
                String initials = initialCrafter.computeInitials(person, options.getGameMode());

                QuizQuestionContext ctx = new QuizQuestionContext.Builder()
                                .withSource(QuizQuestionSource.TRAINING)
                                .withReducedOptionsId(options.getId())
                                .withKnowledgeTracking(trackKnowledge)
                                .build();

                List<Long> candidatePoolIds = List.of();
                if (plan.format() == QuizFormat.MCQ) {
                        List<Long> selectedIds = plan.selectedDistractors().stream()
                                        .map(Person::getId)
                                        .filter(Objects::nonNull)
                                        .toList();
                        candidatePoolIds = selectedIds.isEmpty() ? plan.candidatePoolIds() : selectedIds;
                }

                QuizQuestionSpec spec = new QuizQuestionSpec.Builder()
                                .withSource(QuizQuestionSource.TRAINING)
                                .withPersonId(person.getId())
                                .withStorageKey(storageKey)
                                .withGameModeId(options.getGameMode().getId())
                                .withTargetAttributeIds(targetAttributeIds)
                                .withOperator(operator)
                                .withContext(ctx)
                                .withInitials(initials)
                                .withCandidatePoolPersonIds(candidatePoolIds)
                                .withTimed(plan.timed())
                                .withTimeLimitMs(plan.timeLimitMs())
                                .withReasonCode(plan.reasonCode())
                                .withReasonDetailsJson(plan.reasonDetailsJson())
                                .build();

                QuizQuestion q = questionFactory.build(spec, plan.format());

                // 7) Snapshot + attempt store.
                List<TruthAttributeValue> frozenTruth = snapshotFactory.freezeTruthForQuestion(q);
                QuizQuestionSnapshot snapshot = snapshotFactory.fromQuestion(q, frozenTruth);

                long askedAtEpochMs = System.currentTimeMillis();
                AttemptHandle handle = attemptStore.put(
                                QuizQuestionSource.TRAINING,
                                userService.getCurrentIdOrThrow(),
                                snapshot,
                                askedAtEpochMs,
                                DEFAULT_TRAINING_TTL_SEC);

                if (handle instanceof AttemptHandle.TokenHandle th) {
                        String unifiedHandle = quizHandleCodec.encode(new AttemptRef(QuizQuestionSource.TRAINING, th));
                        q.setQuestionHandle(unifiedHandle);
                } else {
                        throw new IllegalStateException("Expected TokenHandle for TRAINING but got " + handle);
                }

                return q;
        }

        // ------------------------------------------------------------------
        // TRAINING/REVIEW - ANSWER (token-based)
        // ------------------------------------------------------------------

        @Transactional
        protected QuizAnswerResult answerTokenBased(
                        QuizQuestionSource source,
                        AttemptHandle handle,
                        Long userId,
                        QuizAnswerSubmission submission,
                        boolean helpUsed) {

                if (source == null)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "source is required");
                if (source == QuizQuestionSource.COURSE)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "answerTokenBased does not support COURSE");
                if (!(handle instanceof AttemptHandle.TokenHandle))
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "answerTokenBased expects TokenHandle");

                QuizAttemptStore.StoredAttempt stored = attemptStore
                                .peek(source, handle, userId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.BAD_REQUEST,
                                                "Invalid or expired token"));

                QuizQuestionSnapshot snapshot = stored.snapshot();
                SnapshotEvaluation eval = evaluateSnapshot(snapshot, submission);

                QuizValidationResult validation = eval.validation();
                boolean isMultiStep = eval.multiStep();
                Boolean isComplete = eval.isComplete();

                if (isMultiStep && Boolean.FALSE.equals(isComplete)) {

                        QuizQuestionSnapshot updatedSnapshot = eval.updatedSnapshot();
                        String raw = serializeRawSubmission(submission);
                        String normalizedAudit = eval.normalizedAudit();

                        boolean updated = attemptStore.updateAttempt(
                                        source, handle, userId, updatedSnapshot, raw, normalizedAudit);

                        if (!updated) {
                                log.warn("Failed to update question state for source={}, userId={}", source, userId);
                                throw new ResponseStatusException(
                                                HttpStatus.CONFLICT,
                                                "Failed to update question state");
                        }

                        QuizQuestion currentQuestion = QuizQuestionSnapshotMapper.toQuestion(updatedSnapshot);
                        currentQuestion.setQuestionHandle(quizHandleCodec.encode(new AttemptRef(source, handle)));

                        return new QuizAnswerResult.Builder()
                                        .withCorrect(validation != null && validation.isCorrect())
                                        .withFeedbackMessage(eval.feedbackMessage())
                                        .withIsComplete(false)
                                        .withCurrentState(null)
                                        .withNextQuestion(currentQuestion)
                                        .withItemResults(List.of())
                                        .build();
                }

                QuizAttemptStore.StoredAttempt finalStored = attemptStore
                                .consume(source, handle, userId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.BAD_REQUEST,
                                                "Invalid or expired token"));

                QuizQuestionSnapshot effectiveSnapshot = eval.updatedSnapshot();

                long nowEpochMs = System.currentTimeMillis();
                long responseTimeMs = Math.max(0, nowEpochMs - finalStored.askedAtEpochMs());
                java.time.LocalDateTime answeredAt = java.time.LocalDateTime.now();

                Long gameModeId = effectiveSnapshot.getGameModeId();
                Long personId = resolvePrimaryPersonId(effectiveSnapshot);

                if (shouldTrackKnowledge(effectiveSnapshot)
                                && gameModeId != null
                                && personId != null
                                && validation != null) {
                        var user = userService.getCurrentAuthenticatedUserOrThrow();

                        KnowledgeResultEvent event = new KnowledgeResultEvent.Builder()
                                        .withGameModeId(gameModeId)
                                        .withPersonId(personId)
                                        .withCorrect(validation.isCorrect())
                                        .withHelpUsed(helpUsed)
                                        .withOccurredAt(answeredAt)
                                        .build();

                        knowledgeService.recordBatchResults(user, List.of(event));

                        Knowledge knowledge = knowledgeService.findByUserGameModeAndPerson(userId, gameModeId,
                                        personId);

                        if (knowledge != null && knowledge.getId() != null) {
                                knowledgeStatsService.upsertOnAnswer(
                                                userId,
                                                gameModeId,
                                                knowledge.getId(),
                                                personId,
                                                validation.isCorrect(),
                                                helpUsed,
                                                (int) responseTimeMs,
                                                answeredAt);
                        }
                }

                return new QuizAnswerResult.Builder()
                                .withCorrect(validation != null && validation.isCorrect())
                                .withFeedbackMessage(eval.feedbackMessage())
                                .withIsComplete(true)
                                .withCurrentState(extractStateForDto(effectiveSnapshot))
                                .withNextQuestion(null)
                                .withItemResults(List.of(
                                                new QuizAnswerItemResult.Builder()
                                                                .withPosition(0)
                                                                .withRole(QuizQuestionItemRole.TARGET)
                                                                .withKnowledgeId(null)
                                                                .withPersonId(personId)
                                                                .withCorrect(validation != null
                                                                                && validation.isCorrect())
                                                                .withUserAnswerNormalized(submission != null
                                                                                ? submission.getUserAnswer()
                                                                                : null)
                                                                .withCorrectAnswer(validation != null
                                                                                ? validation.getCorrectAnswerDisplay()
                                                                                : null)
                                                                .withResultAttributes(validation != null
                                                                                ? validation.getResultAttributes()
                                                                                : null)
                                                                .build()))
                                .build();
        }

        // ------------------------------------------------------------------
        // Snapshot evaluation (used by training flow, also available for other
        // services)
        // ------------------------------------------------------------------

        public static record SnapshotEvaluation(
                        QuizValidationResult validation,
                        boolean multiStep,
                        Boolean isComplete,
                        QuizQuestionSnapshot updatedSnapshot,
                        String feedbackMessage,
                        String normalizedAudit) {
        }

        public SnapshotEvaluation evaluateSnapshot(QuizQuestionSnapshot snapshot, QuizAnswerSubmission submission) {
                if (snapshot == null) {
                        throw new IllegalArgumentException("snapshot is required");
                }

                QuizEvaluationResult eval = answerValidator.evaluate(snapshot, submission);

                String normalizedAudit = eval.normalizedAudit() != null ? eval.normalizedAudit().auditString() : null;

                boolean multiStep = isMultiStepFormat(snapshot.getFormat());

                QuizQuestionSnapshot updatedSnapshot = eval.updatedSnapshot() != null
                                ? eval.updatedSnapshot()
                                : snapshot;

                QuizValidationResult validation = new QuizValidationResult.Builder()
                                .withCorrect(eval.correct())
                                .withIsComplete(eval.isComplete())
                                .withUpdatedState(eval.updatedState())
                                .build();

                return new SnapshotEvaluation(validation, multiStep, eval.isComplete(), updatedSnapshot,
                                eval.feedbackMessage(), normalizedAudit);
        }

        // ------------------------------------------------------------------
        // Multi-step helpers
        // ------------------------------------------------------------------

        public static boolean isMultiStepFormat(QuizFormat format) {
                return format == QuizFormat.HANGMAN || format == QuizFormat.WORD_PUZZLE;
        }

        public QuizQuestionSnapshot applyStateUpdate(QuizQuestionSnapshot original, MultiStepState updatedState) {
                if (original == null || updatedState == null) {
                        return original;
                }
                if (updatedState instanceof HangmanSnapshotState) {
                        return new QuizQuestionSnapshot.Builder()
                                        .from(original)
                                        .withHangmanState((HangmanSnapshotState) updatedState)
                                        .build();
                } else if (updatedState instanceof WordPuzzleSnapshotState) {
                        return new QuizQuestionSnapshot.Builder()
                                        .from(original)
                                        .withWordPuzzleState((WordPuzzleSnapshotState) updatedState)
                                        .build();
                }
                return original;
        }

        private MultiStepState extractStateForDto(QuizQuestionSnapshot snapshot) {
                if (snapshot == null || snapshot.getFormat() == null) {
                        return null;
                }
                return switch (snapshot.getFormat()) {
                        case HANGMAN -> snapshot.getHangmanState();
                        case WORD_PUZZLE -> snapshot.getWordPuzzleState();
                        default -> null;
                };
        }

        private static Long resolvePrimaryPersonId(QuizQuestionSnapshot snapshot) {
                if (snapshot == null)
                        return null;

                Long personId = snapshot.getPersonId();
                if (personId != null)
                        return personId;

                if (snapshot.getTargetPersonIds() != null && !snapshot.getTargetPersonIds().isEmpty()) {
                        return snapshot.getTargetPersonIds().get(0);
                }
                return null;
        }

        // ------------------------------------------------------------------
        // helpers (audit + candidate selection)
        // ------------------------------------------------------------------

        private static String serializeRawSubmission(QuizAnswerSubmission submission) {
                if (submission == null)
                        return null;

                if (submission.getUserAnswer() != null)
                        return submission.getUserAnswer();
                if (submission.getSelectedChoiceId() != null)
                        return "selectedChoiceId=" + submission.getSelectedChoiceId();
                if (submission.getSelectedChoiceIds() != null)
                        return "selectedChoiceIds=" + submission.getSelectedChoiceIds();
                if (submission.getSwipeRight() != null)
                        return "swipeRight=" + submission.getSwipeRight();
                if (submission.getOrderingIds() != null)
                        return "orderingIds=" + submission.getOrderingIds();
                if (submission.getPairs() != null)
                        return "pairs=" + submission.getPairs();
                if (submission.getTimeMs() != null)
                        return "timeMs=" + submission.getTimeMs();

                return null;
        }

        private static boolean shouldTrackKnowledge(QuizQuestionSnapshot snapshot) {
                if (snapshot == null || snapshot.getContext() == null) {
                        return true;
                }
                Boolean enabled = snapshot.getContext().getKnowledgeTracking();
                return enabled == null || enabled;
        }

        private static String approvedStorageKeyOrThrow(Person person) {
                return person.getPhotos().stream()
                                .filter(p -> p.getStatus() == PhotoStatus.APPROVED)
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException(
                                                "Person " + person.getId() + " has no APPROVED photo"))
                                .getStorageKey();
        }

        private static Person selectTrainingCandidate(List<Person> persons, Long avoidPersonId) {
                if (persons == null || persons.isEmpty()) {
                        return null;
                }
                Person fallback = null;
                for (Person p : persons) {
                        if (!isEligibleCandidate(p)) {
                                continue;
                        }
                        if (fallback == null) {
                                fallback = p;
                        }
                        if (avoidPersonId != null && avoidPersonId.equals(p.getId())) {
                                continue;
                        }
                        return p;
                }
                return fallback;
        }

        private static boolean isEligibleCandidate(Person person) {
                if (person == null || person.getId() == null) {
                        return false;
                }
                return person.getPhotos() != null
                                && person.getPhotos().stream()
                                                .anyMatch(p -> p != null && p.getStatus() == PhotoStatus.APPROVED);
        }

        private static List<QuizPayloadItem> buildCandidateItems(List<Person> persons) {
                if (persons == null || persons.isEmpty()) {
                        return List.of();
                }
                return persons.stream()
                                .filter(Objects::nonNull)
                                .map(p -> {
                                        Long id = p.getId();
                                        if (id == null) {
                                                return null;
                                        }
                                        String storageKey = approvedStorageKeyOrThrow(p);
                                        return new QuizPayloadItem.Builder()
                                                        .withPersonId(id)
                                                        .withStorageKey(storageKey)
                                                        .withLabelId(String.valueOf(id))
                                                        .build();
                                })
                                .filter(Objects::nonNull)
                                .toList();
        }
}
