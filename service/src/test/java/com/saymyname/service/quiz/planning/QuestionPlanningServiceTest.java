package com.saymyname.service.quiz.planning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.quiz.planning.LearnerState;
import com.saymyname.core.model.quiz.planning.LearningObjective;
import com.saymyname.core.model.quiz.planning.PlanningContext;
import com.saymyname.core.model.quiz.planning.PlanningRequest;
import com.saymyname.core.model.quiz.planning.QuestionPlan;
import com.saymyname.core.model.quiz.planning.QuestionPlan.DistractorStrategy;
import com.saymyname.service.quiz.planning.FormatAndCandidateSelector.FormatAndCandidates;

class QuestionPlanningServiceTest {

    @Test
    void multiTargetEligibleUsesOrderingOrAssociation() {
        LearnerStateAssessor assessor = mock(LearnerStateAssessor.class);
        ObjectiveSelector objectiveSelector = mock(ObjectiveSelector.class);
        FormatAndCandidateSelector formatSelector = mock(FormatAndCandidateSelector.class);
        MultiTargetPlanner multiTargetPlanner = mock(MultiTargetPlanner.class);

        QuestionPlanningService service = new QuestionPlanningService(
                assessor,
                objectiveSelector,
                formatSelector,
                multiTargetPlanner);

        LearnerState state = LearnerState.withDefaults();
        when(assessor.assess(any())).thenReturn(state);
        when(objectiveSelector.select(eq(state), any())).thenReturn(LearningObjective.PRACTICE_MASTERED);

        Person primaryPerson = new Person.Builder().withId(1L).build();
        FormatAndCandidates selection = new FormatAndCandidates(
                QuizFormat.ORDERING,
                primaryPerson,
                List.of(),
                List.of(),
                DistractorStrategy.DISTINCT,
                0.0);
        when(formatSelector.select(any(), eq(state), any(), eq(LearningObjective.PRACTICE_MASTERED)))
                .thenReturn(selection);

        Knowledge k1 = new Knowledge.Builder().withId(1L).withPerson(primaryPerson).build();
        Knowledge k2 = new Knowledge.Builder().withId(2L).withPerson(new Person.Builder().withId(2L).build()).build();
        Knowledge k3 = new Knowledge.Builder().withId(3L).withPerson(new Person.Builder().withId(3L).build()).build();
        Knowledge k4 = new Knowledge.Builder().withId(4L).withPerson(new Person.Builder().withId(4L).build()).build();

        MultiTargetPlanner.MultiTargetSelection targetSelection = new MultiTargetPlanner.MultiTargetSelection(
                true,
                4,
                List.of(k1, k2, k3, k4),
                null,
                null);
        when(multiTargetPlanner.selectTargets(any(), eq(true))).thenReturn(targetSelection);

        PlanningContext context = PlanningContext.forCourse(10L);
        PlanningRequest request = PlanningRequest.builder()
                .userId(42L)
                .gameModeId(7L)
                .context(context)
                .primaryKnowledge(k1)
                .build();

        QuestionPlan plan = service.plan(request);

        assertEquals(QuizFormat.ORDERING, plan.format());
        assertTrue(plan.isMultiTarget());
        assertEquals(4, plan.targetCount());
        assertEquals(4, plan.targetKnowledges().size());
    }
}
