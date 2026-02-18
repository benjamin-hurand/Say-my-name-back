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
import com.saymyname.persistence.dao.FactDao;

class QuizQuestionSnapshotFactoryTest {

        @Test
        void mcqSnapshotAlwaysHasTargetAttributeIds() {
                FactDao personAttributeDao = Mockito.mock(FactDao.class);
                QuizQuestionSnapshotFactory factory = new QuizQuestionSnapshotFactory(personAttributeDao);

                QuizQuestionPayload payload = QuizQuestionPayload.builder()
                                .type(QuizPayloadType.MCQ)
                                .choices(List.of(QuizChoice.builder()
                                                .id(1L)
                                                .label("A")
                                                .value("A")
                                                // ✅ IMPORTANT : la factory matche d'abord par personId
                                                // (question.personId=10L)
                                                .personId(10L)
                                                .build()))
                                .allowMultiple(false)
                                .build();

                QuizQuestion question = QuizQuestion.builder()
                                .personId(10L)
                                .storageKey("sk")
                                .gameModeId(1L)
                                .targetAttributeIds(List.of(42L))
                                .operator("op")
                                .context(QuizQuestionContext.builder()
                                                .source(QuizQuestionSource.COURSE)
                                                .courseId(1L)
                                                .courseQuestionId(2L)
                                                .questionRound(1)
                                                .build())
                                .format(QuizFormat.MCQ)
                                .payload(payload)
                                .reasonCode(QuizDecisionReasonCode.FALLBACK_MULTI_TARGET_SHORTFALL)
                                .reasonDetailsJson("{}")
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
                FactDao personAttributeDao = Mockito.mock(FactDao.class);
                QuizQuestionSnapshotFactory factory = new QuizQuestionSnapshotFactory(personAttributeDao);

                Long targetPersonId = 42L;

                // Create MCQ with 4 choices, one matches targetPersonId
                QuizQuestionPayload payload = QuizQuestionPayload.builder()
                                .type(QuizPayloadType.MCQ)
                                .choices(List.of(
                                                QuizChoice.builder()
                                                                .id(1L)
                                                                .label("Marie")
                                                                .value("marie")
                                                                .personId(10L)
                                                                .build(),
                                                QuizChoice.builder()
                                                                .id(2L)
                                                                .label("Jean")
                                                                .value("jean")
                                                                .personId(targetPersonId) // CORRECT
                                                                .build(),
                                                QuizChoice.builder()
                                                                .id(3L)
                                                                .label("Sophie")
                                                                .value("sophie")
                                                                .personId(20L)
                                                                .build(),
                                                QuizChoice.builder()
                                                                .id(4L)
                                                                .label("Paul")
                                                                .value("paul")
                                                                .personId(30L)
                                                                .build()))
                                .allowMultiple(false)
                                .build();

                TruthAttributeValue frozenTruth = TruthAttributeValue.builder()
                                .attributeId(100L)
                                .value("Jean Dupont")
                                .build();

                QuizQuestion question = QuizQuestion.builder()
                                .personId(targetPersonId)
                                .storageKey("photo_jean.jpg")
                                .gameModeId(1L)
                                .targetAttributeIds(List.of(100L))
                                .operator("equals")
                                .context(QuizQuestionContext.builder()
                                                .source(QuizQuestionSource.TRAINING)
                                                .questionRound(1)
                                                .build())
                                .format(QuizFormat.MCQ)
                                .payload(payload)
                                .reasonCode(QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT)
                                .reasonDetailsJson("{}")
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
                FactDao personAttributeDao = Mockito.mock(FactDao.class);
                QuizQuestionSnapshotFactory factory = new QuizQuestionSnapshotFactory(personAttributeDao);

                Long targetPersonId = 42L;

                // Create MCQ with 4 choices, one matches frozenTruthValue
                QuizQuestionPayload payload = QuizQuestionPayload.builder()
                                .type(QuizPayloadType.MCQ)
                                .choices(List.of(
                                                QuizChoice.builder()
                                                                .id(1L)
                                                                .label("Martin")
                                                                .value("Martin")
                                                                .build(),
                                                QuizChoice.builder()
                                                                .id(2L)
                                                                .label("Dupont")
                                                                .value("Dupont") // CORRECT - matches frozen truth
                                                                .build(),
                                                QuizChoice.builder()
                                                                .id(3L)
                                                                .label("Bernard")
                                                                .value("Bernard")
                                                                .build(),
                                                QuizChoice.builder()
                                                                .id(4L)
                                                                .label("Durand")
                                                                .value("Durand")
                                                                .build()))
                                .allowMultiple(false)
                                .build();

                TruthAttributeValue frozenTruth = TruthAttributeValue.builder()
                                .attributeId(100L)
                                .value("Dupont") // Matches choice ID 2
                                .build();

                QuizQuestion question = QuizQuestion.builder()
                                .personId(targetPersonId)
                                .storageKey("photo_jean.jpg")
                                .gameModeId(1L)
                                .targetAttributeIds(List.of(100L))
                                .operator("equals")
                                .context(QuizQuestionContext.builder()
                                                .source(QuizQuestionSource.TRAINING)
                                                .questionRound(1)
                                                .build())
                                .format(QuizFormat.MCQ)
                                .payload(payload)
                                .reasonCode(QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT)
                                .reasonDetailsJson("{}")
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
                FactDao personAttributeDao = Mockito.mock(FactDao.class);
                QuizQuestionSnapshotFactory factory = new QuizQuestionSnapshotFactory(personAttributeDao);

                QuizQuestionPayload payload = QuizQuestionPayload.builder()
                                .type(QuizPayloadType.MCQ)
                                .choices(List.of(
                                                QuizChoice.builder()
                                                                .id(1L)
                                                                .value("DUPONT")
                                                                .build()))
                                .build();

                TruthAttributeValue frozenTruth = TruthAttributeValue.builder()
                                .attributeId(100L)
                                .value("dupont") // Different case
                                .build();

                QuizQuestion question = QuizQuestion.builder()
                                .personId(10L)
                                .gameModeId(1L)
                                .targetAttributeIds(List.of(100L))
                                .operator("equals")
                                .context(QuizQuestionContext.builder()
                                                .source(QuizQuestionSource.TRAINING)
                                                .questionRound(1)
                                                .build())
                                .format(QuizFormat.MCQ)
                                .payload(payload)
                                .reasonCode(QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT)
                                .reasonDetailsJson("{}")
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
                FactDao personAttributeDao = Mockito.mock(FactDao.class);
                QuizQuestionSnapshotFactory factory = new QuizQuestionSnapshotFactory(personAttributeDao);

                QuizQuestionPayload payload = QuizQuestionPayload.builder()
                                .type(QuizPayloadType.MCQ)
                                .choices(List.of(
                                                QuizChoice.builder()
                                                                .id(1L)
                                                                .value("Wrong")
                                                                .personId(99L) // Doesn't match
                                                                .build()))
                                .build();

                TruthAttributeValue frozenTruth = TruthAttributeValue.builder()
                                .attributeId(100L)
                                .value("Correct") // No choice has this value
                                .build();

                QuizQuestion question = QuizQuestion.builder()
                                .personId(42L) // Doesn't match any choice
                                .gameModeId(1L)
                                .targetAttributeIds(List.of(100L))
                                .operator("equals")
                                .context(QuizQuestionContext.builder()
                                                .source(QuizQuestionSource.TRAINING)
                                                .questionRound(1)
                                                .build())
                                .format(QuizFormat.MCQ)
                                .payload(payload)
                                .reasonCode(QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT)
                                .reasonDetailsJson("{}")
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
                FactDao personAttributeDao = Mockito.mock(FactDao.class);
                QuizQuestionSnapshotFactory factory = new QuizQuestionSnapshotFactory(personAttributeDao);

                Long targetPersonId = 42L;

                // Create choices where BOTH personId and value could match
                QuizQuestionPayload payload = QuizQuestionPayload.builder()
                                .type(QuizPayloadType.MCQ)
                                .choices(List.of(
                                                QuizChoice.builder()
                                                                .id(1L)
                                                                .value("Dupont") // Value matches frozen truth
                                                                .personId(99L)
                                                                .build(),
                                                QuizChoice.builder()
                                                                .id(2L)
                                                                .value("Martin")
                                                                .personId(targetPersonId) // PersonId matches
                                                                .build()))
                                .build();

                TruthAttributeValue frozenTruth = TruthAttributeValue.builder()
                                .attributeId(100L)
                                .value("Dupont")
                                .build();

                QuizQuestion question = QuizQuestion.builder()
                                .personId(targetPersonId)
                                .gameModeId(1L)
                                .targetAttributeIds(List.of(100L))
                                .operator("equals")
                                .context(QuizQuestionContext.builder()
                                                .source(QuizQuestionSource.TRAINING)
                                                .questionRound(1)
                                                .build())
                                .format(QuizFormat.MCQ)
                                .payload(payload)
                                .reasonCode(QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT)
                                .reasonDetailsJson("{}")
                                .build();

                // Act
                QuizQuestionSnapshot snapshot = factory.fromQuestion(question, List.of(frozenTruth));

                // Assert - should match by personId (choice 2), not by value (choice 1)
                assertEquals("2", snapshot.getTruth().getCorrectChoiceKeys().get(0));
        }
}
