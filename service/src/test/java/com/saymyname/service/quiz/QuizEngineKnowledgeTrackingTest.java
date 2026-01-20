package com.saymyname.service.quiz;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPayloadType;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.enums.quiz.snapshot.QuizTruthType;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizEvaluationResult;
import com.saymyname.core.model.quiz.QuizQuestionContext;
import com.saymyname.core.model.quiz.QuizQuestionPayload;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionTruth;
import com.saymyname.service.config.BaseServiceTest;
import com.saymyname.service.course.KnowledgeService;
import com.saymyname.service.course.KnowledgeStatsService;
import com.saymyname.service.quiz.handle.QuizHandleCodec;
import com.saymyname.service.quiz.planning.QuestionPlanningService;
import com.saymyname.service.quiz.store.QuizAttemptStore;
import com.saymyname.service.quiz.store.QuizAttemptStore.AttemptHandle;

class QuizEngineKnowledgeTrackingTest extends BaseServiceTest {

    @Test
    void trainingTrackKnowledgeFalseDoesNotUpdateKnowledgeOrStats() {
        QuizAnswerValidator answerValidator = mock(QuizAnswerValidator.class);
        QuizAttemptStore attemptStore = mock(QuizAttemptStore.class);
        KnowledgeService knowledgeService = mock(KnowledgeService.class);
        KnowledgeStatsService knowledgeStatsService = mock(KnowledgeStatsService.class);

        QuizQuestionSnapshot snapshot = buildSnapshot(false);
        Long userId = 42L;
        AttemptHandle.TokenHandle handle = new AttemptHandle.TokenHandle("token");

        QuizAttemptStore.StoredAttempt stored = new QuizAttemptStore.StoredAttempt(
                userId,
                snapshot,
                System.currentTimeMillis(),
                null,
                null,
                null);

        when(attemptStore.peek(QuizQuestionSource.TRAINING, handle, userId))
                .thenReturn(Optional.of(stored));
        when(attemptStore.consume(QuizQuestionSource.TRAINING, handle, userId))
                .thenReturn(Optional.of(stored));

        when(answerValidator.evaluate(eq(snapshot), any()))
                .thenReturn(new QuizEvaluationResult(true, "ok", true, null, null, null));

        QuizEngine engine = new QuizEngine(
                mock(com.saymyname.persistence.dao.PersonDao.class),
                mock(QuizCandidateProvider.class),
                mock(com.saymyname.core.util.InitialCrafter.class),
                mock(QuizQuestionFactory.class),
                answerValidator,
                mock(QuizQuestionSnapshotFactory.class),
                attemptStore,
                knowledgeService,
                knowledgeStatsService,
                mock(QuestionPlanningService.class),
                mock(com.saymyname.service.UserService.class),
                mock(QuizHandleCodec.class));

        QuizAnswerSubmission submission = new QuizAnswerSubmission.Builder()
                .withUserAnswer("answer")
                .build();

        engine.answerTokenBased(QuizQuestionSource.TRAINING, handle, userId, submission, false);

        verifyNoInteractions(knowledgeService);
        verifyNoInteractions(knowledgeStatsService);
    }

    private QuizQuestionSnapshot buildSnapshot(boolean trackKnowledge) {
        QuizQuestionContext context = new QuizQuestionContext.Builder()
                .withSource(QuizQuestionSource.TRAINING)
                .withReducedOptionsId(1L)
                .withKnowledgeTracking(trackKnowledge)
                .build();

        QuizQuestionPayload payload = new QuizQuestionPayload.Builder()
                .withType(QuizPayloadType.TEXT_INPUT)
                .build();

        QuizQuestionTruth truth = new QuizQuestionTruth.Builder()
                .withType(QuizTruthType.TEXT)
                .withExpectedNormalizedAnswers(List.of("answer"))
                .build();

        return new QuizQuestionSnapshot.Builder()
                .withSnapshotSchemaVersion(1)
                .withGeneratorVersion("test")
                .withNormalizerVersion("test")
                .withFormat(QuizFormat.TEXT_INPUT)
                .withContext(context)
                .withGameModeId(1L)
                .withTargetAttributeIds(List.of(10L))
                .withOperator("equals")
                .withPersonId(100L)
                .withPayload(payload)
                .withTruth(truth)
                .withTargetPersonIds(List.of(100L))
                .build();
    }
}
