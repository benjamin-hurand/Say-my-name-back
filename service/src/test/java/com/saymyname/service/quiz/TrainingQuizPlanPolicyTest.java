package com.saymyname.service.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizPreferredFormat;

class TrainingQuizPlanPolicyTest {

    @Test
    void autoDecisionUsesDefaultReasonCode() {
        TrainingQuizPlanPolicy policy = new TrainingQuizPlanPolicy();
        Knowledge knowledge = new Knowledge();
        knowledge.setStatus(KnowledgeStatus.UNKNOWN);

        TrainingQuizPlanPolicy.Plan plan = policy.decide(
                knowledge,
                null,
                QuizPreferredFormat.AUTO,
                null,
                null);

        assertEquals(QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT, plan.reasonCode());
        assertNotNull(plan.reasonDetailsJson());
    }

    @Test
    void explicitDecisionUsesExplicitReasonCode() {
        TrainingQuizPlanPolicy policy = new TrainingQuizPlanPolicy();
        Knowledge knowledge = new Knowledge();
        knowledge.setStatus(KnowledgeStatus.LEARNED);

        TrainingQuizPlanPolicy.Plan plan = policy.decide(
                knowledge,
                null,
                QuizPreferredFormat.TEXT_INPUT,
                null,
                null);

        assertEquals(QuizDecisionReasonCode.TRAINING_EXPLICIT_USER, plan.reasonCode());
        assertNotNull(plan.reasonDetailsJson());
    }
}
