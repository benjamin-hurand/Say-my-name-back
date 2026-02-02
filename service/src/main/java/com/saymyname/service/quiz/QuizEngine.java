// src/main/java/com/saymyname/service/quiz/QuizEngine.java
package com.saymyname.service.quiz;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.exception.quiz.QuizUnprocessableException;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.quiz.QuizAnswerResult;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizEvaluationResult;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.planning.PreparedEmit;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.core.model.quiz.snapshot.TruthAttributeValue;
import com.saymyname.service.quiz.handle.AttemptRef;
import com.saymyname.service.quiz.handle.QuizHandleCodec;
import com.saymyname.service.quiz.store.QuizAttemptStore;
import com.saymyname.service.quiz.store.QuizAttemptStore.AttemptHandle;
import com.saymyname.service.quiz.store.QuizAttemptStore.StoredAttempt;

@Service
public class QuizEngine {

        private final QuizHandleCodec quizHandleCodec;
        private final QuizAttemptStore attemptStore;
        private final QuizQuestionFactory quizQuestionFactory;
        private final QuizQuestionSnapshotFactory snapshotFactory;
        private final QuizAnswerValidator answerValidator;

        public QuizEngine(
                        QuizHandleCodec quizHandleCodec,
                        QuizAttemptStore attemptStore,
                        QuizQuestionFactory quizQuestionFactory,
                        QuizQuestionSnapshotFactory snapshotFactory,
                        QuizAnswerValidator answerValidator) {

                this.quizHandleCodec = Objects.requireNonNull(quizHandleCodec, "quizHandleCodec");
                this.attemptStore = Objects.requireNonNull(attemptStore, "attemptStore");
                this.quizQuestionFactory = Objects.requireNonNull(quizQuestionFactory, "quizQuestionFactory");
                this.snapshotFactory = Objects.requireNonNull(snapshotFactory, "snapshotFactory");
                this.answerValidator = Objects.requireNonNull(answerValidator, "answerValidator");
        }

        // ------------------------------------------------------------------
        // EMIT
        // ------------------------------------------------------------------
        @Transactional
        public QuizQuestion emitQuestion(Long userId, PreparedEmit emit) {
                Objects.requireNonNull(userId, "userId");
                Objects.requireNonNull(emit, "emit");
                Objects.requireNonNull(emit.spec(), "emit.spec");

                QuizQuestion q = quizQuestionFactory.build(emit.spec(), emit.format());

                // Freeze truth at emit time (auditable)
                List<TruthAttributeValue> frozenTruth = snapshotFactory.freezeTruthForQuestion(q);
                QuizQuestionSnapshot snapshot = snapshotFactory.fromQuestion(q, frozenTruth);

                long askedAtEpochMs = System.currentTimeMillis();
                QuizQuestionSource source = emit.spec().getSource();

                AttemptHandle handle = attemptStore.put(
                                source,
                                userId,
                                snapshot,
                                askedAtEpochMs,
                                emit.ttlSeconds());

                String encoded = quizHandleCodec.encode(new AttemptRef(source, handle));
                q.setQuestionHandle(encoded);

                return q;
        }

        // ------------------------------------------------------------------
        // ANSWER
        // ------------------------------------------------------------------
        @Transactional
        public QuizAnswerResult answerQuestion(
                        Long userId,
                        String questionHandle,
                        QuizAnswerSubmission submission,
                        PreparedEmit nextEmitOrNull) {

                Objects.requireNonNull(userId, "userId");
                Objects.requireNonNull(questionHandle, "questionHandle");
                Objects.requireNonNull(submission, "submission");

                AttemptRef ref = quizHandleCodec.decodeOrThrow(questionHandle);

                StoredAttempt stored = attemptStore.peek(ref.source(), ref.handle(), userId)
                                .orElseThrow(() -> new IllegalStateException(
                                                "Attempt not found: handle=" + questionHandle));

                QuizEvaluationResult eval = answerValidator.evaluate(stored.snapshot(), submission);

                // Snapshot + audit à persister (même en finalized !)
                QuizQuestionSnapshot snapshotToPersist = (eval.updatedSnapshot() != null) ? eval.updatedSnapshot()
                                : stored.snapshot();

                // Audit : pour l’instant toString() (tu peux brancher un JSON serializer plus
                // tard)
                String normalizedAuditStr = (eval.normalizedAudit() != null) ? eval.normalizedAudit().toString() : null;

                // ------------------------------
                // MULTI-STEP INCOMPLETE => update attempt, do NOT consume
                // ------------------------------
                if (eval.isMultiStepIncomplete()) {

                        attemptStore.updateAttempt(
                                        ref.source(),
                                        ref.handle(),
                                        userId,
                                        snapshotToPersist,
                                        null, // rawSubmission: optionnel (si tu veux conserver la saisie brute)
                                        normalizedAuditStr);

                        return new QuizAnswerResult.Builder()
                                        .withCorrect(eval.correct())
                                        .withFeedbackMessage(eval.feedbackMessage())
                                        .withIsComplete(eval.isComplete()) // false
                                        .withCurrentState(eval.updatedState())
                                        .withNextQuestion(null)
                                        .build();
                }

                // ------------------------------
                // FINALIZED => update first, then consume
                // ------------------------------
                attemptStore.updateAttempt(
                                ref.source(),
                                ref.handle(),
                                userId,
                                snapshotToPersist,
                                null,
                                normalizedAuditStr);

                attemptStore.consume(ref.source(), ref.handle(), userId)
                                .orElseThrow(() -> new IllegalStateException("Attempt not found or already consumed"));

                QuizQuestion next = null;
                if (nextEmitOrNull != null) {
                        try {
                                next = emitQuestion(userId, nextEmitOrNull);
                        } catch (QuizUnprocessableException e) {
                                // acceptable "no next available"
                                next = null;
                        }
                }

                return new QuizAnswerResult.Builder()
                                .withCorrect(eval.correct())
                                .withFeedbackMessage(eval.feedbackMessage())
                                .withIsComplete(eval.isComplete()) // null (single-shot) or true (multi-step done)
                                .withCurrentState(eval.updatedState()) // état final utile UI
                                .withNextQuestion(next)
                                .build();
        }
}
