package com.saymyname.service.quiz.plugins;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.saymyname.core.model.enums.quiz.snapshot.QuizTruthType;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizValidationResult;
import com.saymyname.core.model.quiz.answer.NormalizedChoice;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionTruth;
import com.saymyname.service.quiz.AnswerKeyService;

class McqPluginValidationTest {

    @Test
    void validateProducesNonNullResultAttributesForValidSnapshot() {
        AnswerKeyService answerKeyService = (personId, targetAttributeIds, operator) ->
                new AnswerKeyService.AnswerKey(false, List.of("A"), "A");
        McqPlugin plugin = new McqPlugin(answerKeyService);

        QuizQuestionTruth truth = new QuizQuestionTruth.Builder()
                .withType(QuizTruthType.MCQ)
                .withCorrectChoiceKeys(List.of("1"))
                .withCorrectAnswerDisplay("A")
                .build();

        QuizQuestionSnapshot snapshot = new QuizQuestionSnapshot();
        snapshot.setTruth(truth);
        snapshot.setTargetAttributeIds(List.of(42L));

        NormalizedChoice normalized = new NormalizedChoice(1L, "A");

        QuizValidationResult validation = assertDoesNotThrow(() ->
                plugin.validate(snapshot, new QuizAnswerSubmission(), normalized));

        assertTrue(validation.getResultAttributes().stream().noneMatch(java.util.Objects::isNull));
    }

    @Test
    void validateThrowsWhenTargetAttributeIdsMissing() {
        AnswerKeyService answerKeyService = (personId, targetAttributeIds, operator) ->
                new AnswerKeyService.AnswerKey(false, List.of("A"), "A");
        McqPlugin plugin = new McqPlugin(answerKeyService);

        QuizQuestionTruth truth = new QuizQuestionTruth.Builder()
                .withType(QuizTruthType.MCQ)
                .withCorrectChoiceKeys(List.of("1"))
                .withCorrectAnswerDisplay("A")
                .build();

        QuizQuestionSnapshot snapshot = new QuizQuestionSnapshot();
        snapshot.setTruth(truth);
        snapshot.setTargetAttributeIds(null);

        NormalizedChoice normalized = new NormalizedChoice(1L, "A");

        assertThrows(IllegalStateException.class, () ->
                plugin.validate(snapshot, new QuizAnswerSubmission(), normalized));
    }
}
