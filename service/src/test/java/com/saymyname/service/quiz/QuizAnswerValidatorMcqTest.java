package com.saymyname.service.quiz;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPayloadType;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.enums.quiz.snapshot.QuizTruthType;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizChoice;
import com.saymyname.core.model.quiz.QuizEvaluationResult;
import com.saymyname.core.model.quiz.QuizQuestionContext;
import com.saymyname.core.model.quiz.QuizQuestionPayload;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionTruth;
import com.saymyname.service.quiz.plugins.McqPlugin;

class QuizAnswerValidatorMcqTest {

        @Test
        void evaluateThrowsWhenTargetAttributeIdsMissing() {
                AnswerKeyService answerKeyService = (personId, targetAttributeIds,
                                operator) -> new AnswerKeyService.AnswerKey(false, List.of("A"), "A");
                QuizQuestionFactory factory = new QuizQuestionFactory(List.of(new McqPlugin(answerKeyService)));
                QuizAnswerValidator validator = new QuizAnswerValidator(factory);

                QuizQuestionTruth truth = QuizQuestionTruth.builder()
                                .type(QuizTruthType.MCQ)
                                .correctChoiceKeys(List.of("1"))
                                .correctAnswerDisplay("A")
                                .build();

                QuizQuestionPayload payload = QuizQuestionPayload.builder()
                                .type(QuizPayloadType.MCQ)
                                .choices(List.of(QuizChoice.builder()
                                                .id(1L)
                                                .label("A")
                                                .value("A")
                                                .build()))
                                .allowMultiple(false)
                                .build();

                QuizQuestionSnapshot snapshot = new QuizQuestionSnapshot();
                snapshot.setSnapshotSchemaVersion(1);
                snapshot.setGeneratorVersion("test");
                snapshot.setNormalizerVersion("test");
                snapshot.setFormat(QuizFormat.MCQ);
                snapshot.setContext(QuizQuestionContext.builder()
                                .source(QuizQuestionSource.COURSE)
                                .courseId(1L)
                                .courseQuestionId(2L)
                                .questionRound(1)
                                .build());
                snapshot.setGameModeId(1L);
                snapshot.setOperator("op");
                snapshot.setPayload(payload);
                snapshot.setTruth(truth);
                snapshot.setTargetPersonIds(List.of(10L));
                snapshot.setTargetAttributeIds(null);

                QuizAnswerSubmission submission = new QuizAnswerSubmission();
                submission.setSelectedChoiceId(1L);

                IllegalStateException ex = assertThrows(IllegalStateException.class,
                                () -> validator.evaluate(snapshot, submission));
                assertTrue(ex.getMessage().contains("courseId=1"));
        }

        @Test
        void evaluateSucceedsWithValidTargetAttributeIds() {
                AnswerKeyService answerKeyService = (personId, targetAttributeIds,
                                operator) -> new AnswerKeyService.AnswerKey(false, List.of("A"), "A");
                QuizQuestionFactory factory = new QuizQuestionFactory(List.of(new McqPlugin(answerKeyService)));
                QuizAnswerValidator validator = new QuizAnswerValidator(factory);

                QuizQuestionTruth truth = QuizQuestionTruth.builder()
                                .type(QuizTruthType.MCQ)
                                .correctChoiceKeys(List.of("1"))
                                .correctAnswerDisplay("A")
                                .build();

                QuizQuestionPayload payload = QuizQuestionPayload.builder()
                                .type(QuizPayloadType.MCQ)
                                .choices(List.of(QuizChoice.builder()
                                                .id(1L)
                                                .label("A")
                                                .value("A")
                                                .build()))
                                .allowMultiple(false)
                                .build();

                QuizQuestionSnapshot snapshot = new QuizQuestionSnapshot();
                snapshot.setSnapshotSchemaVersion(1);
                snapshot.setGeneratorVersion("test");
                snapshot.setNormalizerVersion("test");
                snapshot.setFormat(QuizFormat.MCQ);
                snapshot.setContext(QuizQuestionContext.builder()
                                .source(QuizQuestionSource.COURSE)
                                .courseId(1L)
                                .courseQuestionId(2L)
                                .questionRound(1)
                                .build());
                snapshot.setGameModeId(1L);
                snapshot.setOperator("op");
                snapshot.setPayload(payload);
                snapshot.setTruth(truth);
                snapshot.setTargetPersonIds(List.of(10L));
                snapshot.setTargetAttributeIds(List.of(42L));

                QuizAnswerSubmission submission = new QuizAnswerSubmission();
                submission.setSelectedChoiceId(1L);

                QuizEvaluationResult res = assertDoesNotThrow(() -> validator.evaluate(snapshot, submission));
                assertTrue(res.correct());
        }
}
