package com.saymyname.service.quiz.planning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.candidate.EligibilityStats;
import com.saymyname.core.model.quiz.options.GameMode;
import com.saymyname.core.model.quiz.planning.PlanningDecision;
import com.saymyname.core.model.quiz.planning.PlanningRequest;

/**
 * Unit tests for FormatPlanner D1 ladder logic.
 *
 * D1 Ladder format:
 * - UNKNOWN/DISCOVERED → recognition formats (MCQ, BINARY_SWIPE)
 * - LEARNED → recall formats (TEXT_INPUT, CLOZE)
 * - MASTERED → transfer formats (ASSOCIATION, ORDERING)
 */
class FormatPlannerLadderTest {

    private FormatPlanner formatPlanner;
    private GameMode gameMode;

    @BeforeEach
    void setUp() {
        formatPlanner = new FormatPlanner();
        gameMode = new GameMode.Builder()
                .withId(1L)
                .withTitle("Test GameMode")
                .build();
    }

    // Helper to create stats with enough candidates for all formats
    private EligibilityStats statsForAllFormats() {
        return new EligibilityStats(10, 5, 10); // enough for MCQ, BINARY, ASSOCIATION, ORDERING
    }

    // Helper to create stats with only single candidate (TEXT_INPUT only)
    private EligibilityStats statsForSingleOnly() {
        return new EligibilityStats(1, 0, 1);
    }

    // Helper to create stats with photos only (BINARY_SWIPE feasible, not MCQ)
    private EligibilityStats statsForPhotoOnly() {
        return new EligibilityStats(2, 2, 2); // not enough for MCQ (needs 4)
    }

    @Nested
    @DisplayName("D1: Recognition tier (UNKNOWN/DISCOVERED)")
    class RecognitionTierTests {

        @Test
        @DisplayName("UNKNOWN status prefers MCQ when feasible")
        void unknownStatus_prefersMcq_whenFeasible() {
            PlanningRequest request = PlanningRequest.courseAuto(
                    statsForAllFormats(), gameMode, KnowledgeStatus.UNKNOWN, false, null);

            PlanningDecision decision = formatPlanner.plan(request);

            assertEquals(QuizFormat.MCQ, decision.chosenFormat());
            assertEquals(QuizDecisionReasonCode.LADDER_RECOGNITION_MCQ, decision.reasonCode());
        }

        @Test
        @DisplayName("DISCOVERED status prefers MCQ when feasible")
        void discoveredStatus_prefersMcq_whenFeasible() {
            PlanningRequest request = PlanningRequest.courseAuto(
                    statsForAllFormats(), gameMode, KnowledgeStatus.DISCOVERED, false, null);

            PlanningDecision decision = formatPlanner.plan(request);

            assertEquals(QuizFormat.MCQ, decision.chosenFormat());
            assertEquals(QuizDecisionReasonCode.LADDER_RECOGNITION_MCQ, decision.reasonCode());
        }

        @Test
        @DisplayName("UNKNOWN falls back to BINARY_SWIPE when MCQ not feasible")
        void unknownStatus_fallsBackToBinarySwipe_whenMcqNotFeasible() {
            PlanningRequest request = PlanningRequest.courseAuto(
                    statsForPhotoOnly(), gameMode, KnowledgeStatus.UNKNOWN, false, null);

            PlanningDecision decision = formatPlanner.plan(request);

            assertEquals(QuizFormat.BINARY_SWIPE, decision.chosenFormat());
            assertEquals(QuizDecisionReasonCode.LADDER_RECOGNITION_SWIPE, decision.reasonCode());
        }

        @Test
        @DisplayName("UNKNOWN falls back to TEXT_INPUT when no recognition format feasible")
        void unknownStatus_fallsBackToTextInput_whenNoRecognitionFeasible() {
            PlanningRequest request = PlanningRequest.courseAuto(
                    statsForSingleOnly(), gameMode, KnowledgeStatus.UNKNOWN, false, null);

            PlanningDecision decision = formatPlanner.plan(request);

            assertEquals(QuizFormat.TEXT_INPUT, decision.chosenFormat());
            assertEquals(QuizDecisionReasonCode.LADDER_RECALL_TEXT, decision.reasonCode());
        }
    }

    @Nested
    @DisplayName("D1: Recall tier (LEARNED)")
    class RecallTierTests {

        @Test
        @DisplayName("LEARNED status prefers TEXT_INPUT")
        void learnedStatus_prefersTextInput() {
            PlanningRequest request = PlanningRequest.courseAuto(
                    statsForAllFormats(), gameMode, KnowledgeStatus.LEARNED, false, null);

            PlanningDecision decision = formatPlanner.plan(request);

            assertEquals(QuizFormat.TEXT_INPUT, decision.chosenFormat());
            assertEquals(QuizDecisionReasonCode.LADDER_RECALL_TEXT, decision.reasonCode());
        }

        @Test
        @DisplayName("LEARNED status uses TEXT_INPUT even with single candidate")
        void learnedStatus_usesTextInput_withSingleCandidate() {
            PlanningRequest request = PlanningRequest.courseAuto(
                    statsForSingleOnly(), gameMode, KnowledgeStatus.LEARNED, false, null);

            PlanningDecision decision = formatPlanner.plan(request);

            assertEquals(QuizFormat.TEXT_INPUT, decision.chosenFormat());
            assertEquals(QuizDecisionReasonCode.LADDER_RECALL_TEXT, decision.reasonCode());
        }
    }

    @Nested
    @DisplayName("D1: Transfer tier (MASTERED)")
    class TransferTierTests {

        @Test
        @DisplayName("MASTERED status prefers ASSOCIATION when feasible")
        void masteredStatus_prefersAssociation_whenFeasible() {
            PlanningRequest request = PlanningRequest.courseAuto(
                    statsForAllFormats(), gameMode, KnowledgeStatus.MASTERED, false, null);

            PlanningDecision decision = formatPlanner.plan(request);

            assertEquals(QuizFormat.ASSOCIATION, decision.chosenFormat());
            assertEquals(QuizDecisionReasonCode.LADDER_TRANSFER_ASSOCIATION, decision.reasonCode());
        }

        @Test
        @DisplayName("MASTERED falls back to ORDERING when ASSOCIATION not feasible")
        void masteredStatus_fallsBackToOrdering_whenAssociationNotFeasible() {
            // 5 candidates: enough for ORDERING (4) but not ASSOCIATION (6)
            EligibilityStats stats = new EligibilityStats(5, 3, 5);
            PlanningRequest request = PlanningRequest.courseAuto(
                    stats, gameMode, KnowledgeStatus.MASTERED, false, null);

            PlanningDecision decision = formatPlanner.plan(request);

            assertEquals(QuizFormat.ORDERING, decision.chosenFormat());
            assertEquals(QuizDecisionReasonCode.LADDER_TRANSFER_ORDERING, decision.reasonCode());
        }

        @Test
        @DisplayName("MASTERED falls back to TEXT_INPUT when no transfer format feasible")
        void masteredStatus_fallsBackToTextInput_whenNoTransferFeasible() {
            PlanningRequest request = PlanningRequest.courseAuto(
                    statsForSingleOnly(), gameMode, KnowledgeStatus.MASTERED, false, null);

            PlanningDecision decision = formatPlanner.plan(request);

            assertEquals(QuizFormat.TEXT_INPUT, decision.chosenFormat());
            assertEquals(QuizDecisionReasonCode.LADDER_RECALL_TEXT, decision.reasonCode());
        }
    }

    @Nested
    @DisplayName("TRAINING mode (null knowledgeStatus)")
    class TrainingModeTests {

        @Test
        @DisplayName("Null status uses eligibility-only selection (existing behavior)")
        void nullStatus_usesEligibilityOnlySelection() {
            PlanningRequest request = PlanningRequest.auto(
                    statsForAllFormats(), gameMode, false, null);

            PlanningDecision decision = formatPlanner.plan(request);

            // Should use existing priority: MCQ first
            assertEquals(QuizFormat.MCQ, decision.chosenFormat());
            assertEquals(QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT, decision.reasonCode());
        }

        @Test
        @DisplayName("TRAINING mode with single candidate uses TEXT_INPUT")
        void trainingMode_withSingleCandidate_usesTextInput() {
            PlanningRequest request = PlanningRequest.auto(
                    statsForSingleOnly(), gameMode, false, null);

            PlanningDecision decision = formatPlanner.plan(request);

            assertEquals(QuizFormat.TEXT_INPUT, decision.chosenFormat());
            assertEquals(QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT, decision.reasonCode());
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Empty stats throws exception")
        void emptyStats_throwsException() {
            PlanningRequest request = PlanningRequest.courseAuto(
                    EligibilityStats.empty(), gameMode, KnowledgeStatus.UNKNOWN, false, null);

            assertThrows(Exception.class, () -> formatPlanner.plan(request));
        }

        @Test
        @DisplayName("Timed flag is preserved in request")
        void timedFlag_isPreservedInRequest() {
            PlanningRequest request = PlanningRequest.courseAuto(
                    statsForAllFormats(), gameMode, KnowledgeStatus.LEARNED, true, 5000);

            // Just verify the request was created correctly
            assertEquals(true, request.timed());
            assertEquals(5000, request.timeLimitMs());
        }
    }
}
