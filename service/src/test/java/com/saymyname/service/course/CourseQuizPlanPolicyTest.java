package com.saymyname.service.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.saymyname.core.model.course.CourseRecentStats;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.course.KnowledgeStats;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPreferredFormat;

class CourseQuizPlanPolicyTest {

    @Test
    void multiTargetGateAcceptsWhenAllTargetsHealthy() {
        CourseQuizPlanPolicy policy = new CourseQuizPlanPolicy();
        Knowledge knowledge = masteredKnowledge();

        KnowledgeStats primary = stats(3, 3, 0.2, 5000, 0);
        List<KnowledgeStats> extraStats = List.of(
                stats(2, 2, 0.3, 4500, 0),
                stats(4, 4, 0.1, 5200, 0),
                stats(3, 3, 0.0, 3000, 0));

        CourseQuizPlanPolicy.Plan plan = policy.decide(
                null,
                null,
                knowledge,
                primary,
                extraStats,
                null,
                QuizPreferredFormat.AUTO,
                null,
                null,
                true);

        assertEquals(QuizFormat.ASSOCIATION, plan.format());
        assertEquals(4, plan.targetCount());
        assertEquals(QuizDecisionReasonCode.AUTO_MASTERED_MULTI_TARGET, plan.reasonCode());
        assertNotNull(plan.reasonDetailsJson());
    }

    @Test
    void multiTargetGateFallsBackWhenExtraStatsTooRisky() {
        CourseQuizPlanPolicy policy = new CourseQuizPlanPolicy();
        Knowledge knowledge = masteredKnowledge();

        KnowledgeStats primary = stats(3, 3, 0.2, 5000, 0);
        List<KnowledgeStats> extraStats = List.of(
                stats(2, 2, 0.3, 4500, 0),
                stats(4, 4, 0.1, 5200, 2),
                stats(3, 3, 0.0, 3000, 0));

        CourseQuizPlanPolicy.Plan plan = policy.decide(
                null,
                null,
                knowledge,
                primary,
                extraStats,
                null,
                QuizPreferredFormat.AUTO,
                null,
                null,
                true);

        assertEquals(QuizFormat.CLOZE, plan.format());
        assertEquals(1, plan.targetCount());
        assertEquals(QuizDecisionReasonCode.FALLBACK_MULTI_TARGET_GATE, plan.reasonCode());
        assertNotNull(plan.reasonDetailsJson());
    }

    @Test
    void antiMonotonyVariationAppliedWhenNotUnderStress() {
        CourseQuizPlanPolicy policy = new CourseQuizPlanPolicy();
        Knowledge knowledge = masteredKnowledge();

        KnowledgeStats primary = stats(3, 3, 0.0, 5000, 0);
        CourseRecentStats courseStats = new CourseRecentStats.Builder()
                .withFormatStreak(3)
                .withLastFormat(QuizFormat.CLOZE)
                .withErrorStreak(0)
                .withHelpStreak(0)
                .withAvgRtRecent(5000)
                .build();

        CourseQuizPlanPolicy.Plan plan = policy.decide(
                null,
                null,
                knowledge,
                primary,
                List.of(),
                courseStats,
                QuizPreferredFormat.AUTO,
                null,
                null,
                false);

        assertEquals(QuizFormat.TEXT_INPUT, plan.format());
        assertEquals(QuizDecisionReasonCode.ANTI_MONOTONY_FORMAT_VARIATION, plan.reasonCode());
        assertNotNull(plan.reasonDetailsJson());
    }

    private static Knowledge masteredKnowledge() {
        Knowledge knowledge = new Knowledge();
        knowledge.setStatus(KnowledgeStatus.MASTERED);
        knowledge.setSrsStreak(3);
        knowledge.setFailureCount(0);
        knowledge.setSuccessCount(10);
        return knowledge;
    }

    private static KnowledgeStats stats(
            double attemptsRecent,
            double correctRecent,
            double helpRecent,
            double avgRtRecent,
            int errorStreak) {
        return new KnowledgeStats.Builder()
                .withAttemptsRecent(attemptsRecent)
                .withCorrectRecent(correctRecent)
                .withHelpRecent(helpRecent)
                .withAvgRtRecent(avgRtRecent)
                .withErrorStreak(errorStreak)
                .build();
    }
}
