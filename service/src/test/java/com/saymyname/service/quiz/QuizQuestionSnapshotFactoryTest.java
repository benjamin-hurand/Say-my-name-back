package com.saymyname.service.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPayloadType;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.quiz.QuizChoice;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionContext;
import com.saymyname.core.model.quiz.QuizQuestionPayload;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.core.model.quiz.snapshot.TruthAttributeValue;
import com.saymyname.persistence.dao.PersonAttributeDao;

class QuizQuestionSnapshotFactoryTest {

        @Test
        void mcqSnapshotAlwaysHasTargetAttributeIds() {
                PersonAttributeDao personAttributeDao = Mockito.mock(PersonAttributeDao.class);
                QuizQuestionSnapshotFactory factory = new QuizQuestionSnapshotFactory(personAttributeDao);

                QuizQuestionPayload payload = new QuizQuestionPayload.Builder()
                                .withType(QuizPayloadType.MCQ)
                                .withChoices(List.of(new QuizChoice.Builder()
                                                .withId(1L)
                                                .withLabel("A")
                                                .withValue("A")
                                                // ✅ IMPORTANT : la factory matche d'abord par personId
                                                // (question.personId=10L)
                                                .withPersonId(10L)
                                                .build()))
                                .withAllowMultiple(false)
                                .build();

                QuizQuestion question = new QuizQuestion.Builder()
                                .withPersonId(10L)
                                .withStorageKey("sk")
                                .withGameModeId(1L)
                                .withTargetAttributeIds(List.of(42L))
                                .withOperator("op")
                                .withContext(new QuizQuestionContext.Builder()
                                                .withSource(QuizQuestionSource.COURSE)
                                                .withCourseId(1L)
                                                .withCourseQuestionId(2L)
                                                .withQuestionRound(1)
                                                .build())
                                .withFormat(QuizFormat.MCQ)
                                .withPayload(payload)
                                .withReasonCode(QuizDecisionReasonCode.FALLBACK_MULTI_TARGET_SHORTFALL)
                                .withReasonDetailsJson("{}")
                                .build();

                // Note : ici tu passes frozenTruth vide, mais c'est OK si une choice match par
                // personId.
                QuizQuestionSnapshot snapshot = factory.fromQuestion(question, List.of(), List.of(10L));

                assertNotNull(snapshot.getTargetAttributeIds());
                assertFalse(snapshot.getTargetAttributeIds().isEmpty());
        }

        /**
         * Test MCQ truth extraction by personId matching (photo MCQ: "Who is this?")
         */
        @Test
        void mcqTruthExtraction_matchByPersonId() {
                PersonAttributeDao personAttributeDao = Mockito.mock(PersonAttributeDao.class);
                QuizQuestionSnapshotFactory factory = new QuizQuestionSnapshotFactory(personAttributeDao);

                Long targetPersonId = 42L;

                // Create MCQ with 4 choices, one matches targetPersonId
                QuizQuestionPayload payload = new QuizQuestionPayload.Builder()
                                .withType(QuizPayloadType.MCQ)
                                .withChoices(List.of(
                                                new QuizChoice.Builder()
                                                                .withId(1L)
                                                                .withLabel("Marie")
                                                                .withValue("marie")
                                                                .withPersonId(10L)
                                                                .build(),
                                                new QuizChoice.Builder()
                                                                .withId(2L)
                                                                .withLabel("Jean")
                                                                .withValue("jean")
                                                                .withPersonId(targetPersonId) // CORRECT
                                                                .build(),
                                                new QuizChoice.Builder()
                                                                .withId(3L)
                                                                .withLabel("Sophie")
                                                                .withValue("sophie")
                                                                .withPersonId(20L)
                                                                .build(),
                                                new QuizChoice.Builder()
                                                                .withId(4L)
                                                                .withLabel("Paul")
                                                                .withValue("paul")
                                                                .withPersonId(30L)
                                                                .build()))
                                .withAllowMultiple(false)
                                .build();

                TruthAttributeValue frozenTruth = new TruthAttributeValue.Builder()
                                .withAttributeId(100L)
                                .withValue("Jean Dupont")
                                .build();

                QuizQuestion question = new QuizQuestion.Builder()
                                .withPersonId(targetPersonId)
                                .withStorageKey("photo_jean.jpg")
                                .withGameModeId(1L)
                                .withTargetAttributeIds(List.of(100L))
                                .withOperator("equals")
                                .withContext(new QuizQuestionContext.Builder()
                                                .withSource(QuizQuestionSource.TRAINING)
                                                .withQuestionRound(1)
                                                .build())
                                .withFormat(QuizFormat.MCQ)
                                .withPayload(payload)
                                .withReasonCode(QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT)
                                .withReasonDetailsJson("{}")
                                .build();

                // Act
                QuizQuestionSnapshot snapshot = factory.fromQuestion(question, List.of(frozenTruth));

                // Assert
                assertNotNull(snapshot.getTruth());
                assertNotNull(snapshot.getTruth().getCorrectChoiceKeys());
                assertEquals(1, snapshot.getTruth().getCorrectChoiceKeys().size());
                assertEquals("2", snapshot.getTruth().getCorrectChoiceKeys().get(0)); // Choice ID 2
        }

        /**
         * Test MCQ truth extraction by value matching (text MCQ: "What is Jean's last
         * name?")
         */
        @Test
        void mcqTruthExtraction_matchByValue() {
                PersonAttributeDao personAttributeDao = Mockito.mock(PersonAttributeDao.class);
                QuizQuestionSnapshotFactory factory = new QuizQuestionSnapshotFactory(personAttributeDao);

                Long targetPersonId = 42L;

                // Create MCQ with 4 choices, one matches frozenTruthValue
                QuizQuestionPayload payload = new QuizQuestionPayload.Builder()
                                .withType(QuizPayloadType.MCQ)
                                .withChoices(List.of(
                                                new QuizChoice.Builder()
                                                                .withId(1L)
                                                                .withLabel("Martin")
                                                                .withValue("Martin")
                                                                .build(),
                                                new QuizChoice.Builder()
                                                                .withId(2L)
                                                                .withLabel("Dupont")
                                                                .withValue("Dupont") // CORRECT - matches frozen truth
                                                                .build(),
                                                new QuizChoice.Builder()
                                                                .withId(3L)
                                                                .withLabel("Bernard")
                                                                .withValue("Bernard")
                                                                .build(),
                                                new QuizChoice.Builder()
                                                                .withId(4L)
                                                                .withLabel("Durand")
                                                                .withValue("Durand")
                                                                .build()))
                                .withAllowMultiple(false)
                                .build();

                TruthAttributeValue frozenTruth = new TruthAttributeValue.Builder()
                                .withAttributeId(100L)
                                .withValue("Dupont") // Matches choice ID 2
                                .build();

                QuizQuestion question = new QuizQuestion.Builder()
                                .withPersonId(targetPersonId)
                                .withStorageKey("photo_jean.jpg")
                                .withGameModeId(1L)
                                .withTargetAttributeIds(List.of(100L))
                                .withOperator("equals")
                                .withContext(new QuizQuestionContext.Builder()
                                                .withSource(QuizQuestionSource.TRAINING)
                                                .withQuestionRound(1)
                                                .build())
                                .withFormat(QuizFormat.MCQ)
                                .withPayload(payload)
                                .withReasonCode(QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT)
                                .withReasonDetailsJson("{}")
                                .build();

                // Act
                QuizQuestionSnapshot snapshot = factory.fromQuestion(question, List.of(frozenTruth));

                // Assert
                assertNotNull(snapshot.getTruth());
                assertNotNull(snapshot.getTruth().getCorrectChoiceKeys());
                assertEquals(1, snapshot.getTruth().getCorrectChoiceKeys().size());
                assertEquals("2", snapshot.getTruth().getCorrectChoiceKeys().get(0)); // Choice ID 2
        }

        /**
         * Test MCQ truth extraction with case-insensitive value matching
         */
        @Test
        void mcqTruthExtraction_matchByValue_caseInsensitive() {
                PersonAttributeDao personAttributeDao = Mockito.mock(PersonAttributeDao.class);
                QuizQuestionSnapshotFactory factory = new QuizQuestionSnapshotFactory(personAttributeDao);

                QuizQuestionPayload payload = new QuizQuestionPayload.Builder()
                                .withType(QuizPayloadType.MCQ)
                                .withChoices(List.of(
                                                new QuizChoice.Builder()
                                                                .withId(1L)
                                                                .withValue("DUPONT")
                                                                .build()))
                                .build();

                TruthAttributeValue frozenTruth = new TruthAttributeValue.Builder()
                                .withAttributeId(100L)
                                .withValue("dupont") // Different case
                                .build();

                QuizQuestion question = new QuizQuestion.Builder()
                                .withPersonId(10L)
                                .withGameModeId(1L)
                                .withTargetAttributeIds(List.of(100L))
                                .withOperator("equals")
                                .withContext(new QuizQuestionContext.Builder()
                                                .withSource(QuizQuestionSource.TRAINING)
                                                .withQuestionRound(1)
                                                .build())
                                .withFormat(QuizFormat.MCQ)
                                .withPayload(payload)
                                .withReasonCode(QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT)
                                .withReasonDetailsJson("{}")
                                .build();

                // Act
                QuizQuestionSnapshot snapshot = factory.fromQuestion(question, List.of(frozenTruth));

                // Assert - should match despite case difference
                assertNotNull(snapshot.getTruth().getCorrectChoiceKeys());
                assertEquals(1, snapshot.getTruth().getCorrectChoiceKeys().size());
                assertEquals("1", snapshot.getTruth().getCorrectChoiceKeys().get(0));
        }

        /**
         * Test MCQ truth extraction fails when no matches found
         */
        @Test
        void mcqTruthExtraction_noMatch_throwsException() {
                PersonAttributeDao personAttributeDao = Mockito.mock(PersonAttributeDao.class);
                QuizQuestionSnapshotFactory factory = new QuizQuestionSnapshotFactory(personAttributeDao);

                QuizQuestionPayload payload = new QuizQuestionPayload.Builder()
                                .withType(QuizPayloadType.MCQ)
                                .withChoices(List.of(
                                                new QuizChoice.Builder()
                                                                .withId(1L)
                                                                .withValue("Wrong")
                                                                .withPersonId(99L) // Doesn't match
                                                                .build()))
                                .build();

                TruthAttributeValue frozenTruth = new TruthAttributeValue.Builder()
                                .withAttributeId(100L)
                                .withValue("Correct") // No choice has this value
                                .build();

                QuizQuestion question = new QuizQuestion.Builder()
                                .withPersonId(42L) // Doesn't match any choice
                                .withGameModeId(1L)
                                .withTargetAttributeIds(List.of(100L))
                                .withOperator("equals")
                                .withContext(new QuizQuestionContext.Builder()
                                                .withSource(QuizQuestionSource.TRAINING)
                                                .withQuestionRound(1)
                                                .build())
                                .withFormat(QuizFormat.MCQ)
                                .withPayload(payload)
                                .withReasonCode(QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT)
                                .withReasonDetailsJson("{}")
                                .build();

                // Act & Assert
                IllegalStateException exception = assertThrows(
                                IllegalStateException.class,
                                () -> factory.fromQuestion(question, List.of(frozenTruth)));

                assertTrue(exception.getMessage().contains("MCQ truth extraction failed"));
                assertTrue(exception.getMessage().contains("personId=42"));
                assertTrue(exception.getMessage().contains("Correct"));
        }

        /**
         * Test MCQ truth extraction prioritizes personId over value matching
         */
        @Test
        void mcqTruthExtraction_personIdTakesPrecedence() {
                PersonAttributeDao personAttributeDao = Mockito.mock(PersonAttributeDao.class);
                QuizQuestionSnapshotFactory factory = new QuizQuestionSnapshotFactory(personAttributeDao);

                Long targetPersonId = 42L;

                // Create choices where BOTH personId and value could match
                QuizQuestionPayload payload = new QuizQuestionPayload.Builder()
                                .withType(QuizPayloadType.MCQ)
                                .withChoices(List.of(
                                                new QuizChoice.Builder()
                                                                .withId(1L)
                                                                .withValue("Dupont") // Value matches frozen truth
                                                                .withPersonId(99L)
                                                                .build(),
                                                new QuizChoice.Builder()
                                                                .withId(2L)
                                                                .withValue("Martin")
                                                                .withPersonId(targetPersonId) // PersonId matches
                                                                .build()))
                                .build();

                TruthAttributeValue frozenTruth = new TruthAttributeValue.Builder()
                                .withAttributeId(100L)
                                .withValue("Dupont")
                                .build();

                QuizQuestion question = new QuizQuestion.Builder()
                                .withPersonId(targetPersonId)
                                .withGameModeId(1L)
                                .withTargetAttributeIds(List.of(100L))
                                .withOperator("equals")
                                .withContext(new QuizQuestionContext.Builder()
                                                .withSource(QuizQuestionSource.TRAINING)
                                                .withQuestionRound(1)
                                                .build())
                                .withFormat(QuizFormat.MCQ)
                                .withPayload(payload)
                                .withReasonCode(QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT)
                                .withReasonDetailsJson("{}")
                                .build();

                // Act
                QuizQuestionSnapshot snapshot = factory.fromQuestion(question, List.of(frozenTruth));

                // Assert - should match by personId (choice 2), not by value (choice 1)
                assertEquals("2", snapshot.getTruth().getCorrectChoiceKeys().get(0));
        }
}
