package com.saymyname.service.quiz;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.Test;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPreferredFormat;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.enums.quiz.snapshot.QuizTruthType;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizChoice;
import com.saymyname.core.model.quiz.QuizQuestionContext;
import com.saymyname.core.model.quiz.QuizQuestionPayload;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionTruth;
import com.saymyname.core.model.quiz.snapshot.TruthAttributeValue;
import com.saymyname.core.model.quiz.answer.NormalizedSubmission;
import com.saymyname.core.model.quiz.answer.NormalizedText;
import com.saymyname.persistence.dao.PersonAttributeDao;
import com.saymyname.service.quiz.plugins.McqPlugin;
import com.saymyname.service.quiz.plugins.TextInputPlugin;

class QuizSnapshotValidationTest {

    private static AnswerKeyService stubAnswerKey(String value) {
        return (personId, targetAttributeIds, operator) -> new AnswerKeyService.AnswerKey(false, List.of(value), value);
    }

    @Test
    void snapshotDoesNotRequireToken() {
        QuizQuestionTruth truth = new QuizQuestionTruth.Builder()
                .withType(QuizTruthType.TEXT)
                .withTargetAttributeValues(List.of(new TruthAttributeValue.Builder()
                        .withAttributeId(1L)
                        .withValue("john")
                        .build()))
                .withCorrectAnswerDisplay("john")
                .build();

        QuizQuestionSnapshot snap = new QuizQuestionSnapshot.Builder()
                .withSnapshotSchemaVersion(3)
                .withGeneratorVersion("test-gen")
                .withNormalizerVersion("test-norm")
                .withFormat(QuizFormat.TEXT_INPUT)
                .withContext(new QuizQuestionContext.Builder().withSource(QuizQuestionSource.TRAINING).build())
                .withGameModeId(1L)
                .withTargetAttributeIds(List.of(1L))
                .withOperator("AND")
                .withPersonId(1L)
                .withStorageKey("s")
                .withPayload(new QuizQuestionPayload())
                .withDisplay(null)
                .withHints(null)
                .withFollowUp(null)
                .withTruth(truth)
                .withTargetPersonIds(List.of(1L))
                .build();

        assertNotNull(snap);
        assertEquals(3, snap.getSnapshotSchemaVersion());

        NormalizedSubmission norm = new NormalizedText("John", "john");
        assertEquals("john", norm.auditString());
    }

    @Test
    void mcqSnapshotValidationFromTruth() {
        McqPlugin plugin = new McqPlugin(stubAnswerKey("CORRECT"));
        QuizQuestionSpec spec = new QuizQuestionSpec.Builder()
                .withSource(QuizQuestionSource.TRAINING)
                .withPersonId(1L)
                .withStorageKey("s")
                .withGameModeId(1L)
                .withTargetAttributeIds(List.of(1L))
                .withOperator("AND")
                .withContext(new QuizQuestionContext.Builder().withSource(QuizQuestionSource.TRAINING).build())
                .build();

        var question = plugin.build(spec);

        QuizQuestionTruth truth = new QuizQuestionTruth.Builder()
                .withType(QuizTruthType.MCQ)
                .withCorrectChoiceKeys(question.getPayload().getChoices().stream()
                        .filter(c -> Boolean.TRUE.equals(c.getCorrect()))
                        .map(c -> String.valueOf(c.getId()))
                        .toList())
                .withCorrectAnswerDisplay(question.getPayload().getChoices().stream()
                        .filter(c -> Boolean.TRUE.equals(c.getCorrect()))
                        .map(QuizChoice::getLabel)
                        .findFirst()
                        .orElse(null))
                .build();

        QuizQuestionSnapshot snapshot = new QuizQuestionSnapshot.Builder()
                .withSnapshotSchemaVersion(3)
                .withGeneratorVersion("test-gen")
                .withNormalizerVersion("test-norm")
                .withFormat(question.getFormat())
                .withContext(question.getContext())
                .withGameModeId(question.getGameModeId())
                .withTargetAttributeIds(question.getTargetAttributeIds())
                .withOperator(question.getOperator())
                .withPersonId(question.getPersonId())
                .withStorageKey(question.getStorageKey())
                .withDisplay(question.getDisplay())
                .withHints(question.getHints())
                .withPayload(question.getPayload())
                .withFollowUp(question.getFollowUp())
                .withTruth(truth)
                .withTargetPersonIds(List.of(question.getPersonId()))
                .build();

        // correct choice is first with correct=true
        Long correctId = question.getPayload().getChoices().stream()
                .filter(c -> Boolean.TRUE.equals(c.getCorrect()))
                .findFirst()
                .map(c -> c.getId())
                .orElseThrow();

        QuizAnswerSubmission subOk = new QuizAnswerSubmission();
        subOk.setSelectedChoiceId(correctId);

        var normOk = plugin.normalize(question, subOk);
        var resOk = plugin.validate(snapshot, subOk, normOk);
        assertTrue(resOk.isCorrect());

        QuizAnswerSubmission subKo = new QuizAnswerSubmission();
        subKo.setSelectedChoiceId(correctId + 1);
        var resKo = plugin.validate(snapshot, subKo, plugin.normalize(question, subKo));
        assertFalse(resKo.isCorrect());
    }

    @Test
    void timedTextInputSnapshotAndEvaluate() {
        QuizQuestionFactory factory = factory(stubAnswerKey("JOHN"));
        QuizAnswerValidator validator = new QuizAnswerValidator(factory);
        QuizQuestionSnapshotFactory snapshotFactory = new QuizQuestionSnapshotFactory(mock(PersonAttributeDao.class));

        QuizQuestionSpec spec = baseSpecBuilder()
                .withTimed(true)
                .withTimeLimitMs(8000)
                .build();
        var question = factory.build(spec, QuizPreferredFormat.TEXT_INPUT);

        List<TruthAttributeValue> frozen = List.of(new TruthAttributeValue.Builder()
                .withAttributeId(1L)
                .withValue("john")
                .build());

        QuizQuestionSnapshot snapshot = snapshotFactory.fromQuestion(question, frozen);
        assertTrue(snapshot.getTimed());
        assertEquals(8000, snapshot.getTimeLimitMs());

        QuizAnswerSubmission sub = new QuizAnswerSubmission();
        sub.setUserAnswer("John");

        var eval = validator.evaluateFromSnapshot(snapshot, sub);
        assertTrue(eval.validation().isCorrect());
        assertEquals("john", eval.normalized().auditString());
    }

    @Test
    void snapshotRequiresTimeLimitWhenTimed() {
        QuizQuestionTruth truth = new QuizQuestionTruth.Builder()
                .withType(QuizTruthType.TEXT)
                .withTargetAttributeValues(List.of(new TruthAttributeValue.Builder()
                        .withAttributeId(1L)
                        .withValue("john")
                        .build()))
                .withCorrectAnswerDisplay("john")
                .build();

        assertThrows(IllegalStateException.class, () -> new QuizQuestionSnapshot.Builder()
                .withSnapshotSchemaVersion(3)
                .withGeneratorVersion("test-gen")
                .withNormalizerVersion("test-norm")
                .withFormat(QuizFormat.TEXT_INPUT)
                .withContext(new QuizQuestionContext.Builder().withSource(QuizQuestionSource.TRAINING).build())
                .withGameModeId(1L)
                .withTargetAttributeIds(List.of(1L))
                .withOperator("AND")
                .withPersonId(1L)
                .withStorageKey("s")
                .withPayload(new QuizQuestionPayload())
                .withDisplay(null)
                .withHints(null)
                .withFollowUp(null)
                .withTruth(truth)
                .withTimed(true)
                .withTimeLimitMs(null)
                .withTargetPersonIds(List.of(1L))
                .build());
    }

    private static QuizQuestionFactory factory(AnswerKeyService aks) {
        TextInputPlugin textInputPlugin = new TextInputPlugin();
        McqPlugin mcqPlugin = new McqPlugin(aks);
        return new QuizQuestionFactory(List.of(textInputPlugin, mcqPlugin));
    }

    private static QuizQuestionSpec.Builder baseSpecBuilder() {
        return new QuizQuestionSpec.Builder()
                .withSource(QuizQuestionSource.TRAINING)
                .withPersonId(1L)
                .withStorageKey("s")
                .withGameModeId(1L)
                .withTargetAttributeIds(List.of(1L))
                .withOperator("AND")
                .withContext(new QuizQuestionContext.Builder().withSource(QuizQuestionSource.TRAINING).build());
    }
}
