package com.saymyname.service.course;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionAttempt;
import com.saymyname.core.model.course.CourseQuestionItem;
import com.saymyname.core.model.course.CourseQuestionPlan;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.PoolType;
import com.saymyname.core.model.enums.course.QuizQuestionItemRole;
import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.Photo;
import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.options.GameMode;
import com.saymyname.core.model.quiz.options.GameModeAttribute;
import com.saymyname.core.model.quiz.options.GameOptions;
import com.saymyname.service.quiz.CourseOptionsResolver;
import com.saymyname.service.quiz.QuizCandidateProvider;
import com.saymyname.service.quiz.QuizQuestionFactory;
import com.saymyname.service.quiz.plugins.QuizQuestionPlugin;

class CourseQuizQuestionBuilderTest {

    @Test
    void mcqUsesPlannedDistractorsWithoutRequery() {
        QuizCandidateProvider candidateProvider = mock(QuizCandidateProvider.class);
        QuizQuestionFactory questionFactory = mock(QuizQuestionFactory.class);
        CourseOptionsResolver optionsResolver = mock(CourseOptionsResolver.class);
        QuizQuestionPlugin plugin = mock(QuizQuestionPlugin.class);

        when(questionFactory.pluginFor(QuizFormat.MCQ)).thenReturn(plugin);
        when(plugin.build(any())).thenReturn(new QuizQuestion());

        GameMode gameMode = new GameMode.Builder()
                .withId(1L)
                .withOperator("AND")
                .withGameModeAttributes(List.of(new GameModeAttribute.Builder()
                        .withAttribute(new Attribute.Builder().withId(10L).withPrimaryField(true).build())
                        .build()))
                .build();

        GameOptions options = new GameOptions.Builder()
                .withId(99L)
                .withGameMode(gameMode)
                .build();

        when(optionsResolver.resolve(any())).thenReturn(options);

        CourseQuizQuestionBuilder builder = new CourseQuizQuestionBuilder(
                candidateProvider,
                questionFactory,
                optionsResolver);

        Person targetPerson = personWithPhoto(100L, "photo_target");
        Person distractor1 = personWithPhoto(200L, "photo_d1");
        Person distractor2 = personWithPhoto(300L, "photo_d2");

        Knowledge knowledge = new Knowledge.Builder()
                .withId(500L)
                .withPerson(targetPerson)
                .build();

        Course course = new Course.Builder()
                .withId(1L)
                .withUser(new User.Builder().withId(42L).build())
                .withGameMode(gameMode)
                .build();

        List<CourseQuestionItem> items = List.of(
                new CourseQuestionItem.Builder()
                        .withPosition(0)
                        .withRole(QuizQuestionItemRole.TARGET)
                        .withKnowledge(knowledge)
                        .withPerson(targetPerson)
                        .withAnswered(false)
                        .build(),
                new CourseQuestionItem.Builder()
                        .withPosition(1)
                        .withRole(QuizQuestionItemRole.DISTRACTOR)
                        .withKnowledge(null)
                        .withPerson(distractor1)
                        .withAnswered(false)
                        .build(),
                new CourseQuestionItem.Builder()
                        .withPosition(2)
                        .withRole(QuizQuestionItemRole.DISTRACTOR)
                        .withKnowledge(null)
                        .withPerson(distractor2)
                        .withAnswered(false)
                        .build());

        CourseQuestionAttempt attempt = new CourseQuestionAttempt.Builder()
                .withId(10L)
                .withCourse(course)
                .withQuestionRound(1)
                .withAskedAt(LocalDateTime.now())
                .withResponseTimeMs(0)
                .withPoolType(PoolType.NEW)
                .withItems(items)
                .build();

        CourseQuestionPlan plan = new CourseQuestionPlan.Builder()
                .withFormat(QuizFormat.MCQ)
                .withTimed(false)
                .withTargetCount(1)
                .withTargetKnowledgeIds(List.of(knowledge.getId()))
                .withParamsJson("{\"nbChoices\":4}")
                .withReasonCode(QuizDecisionReasonCode.EXPLICIT_USER)
                .withReasonDetailsJson("{}")
                .build();

        builder.buildFromAttempt(attempt, plan);

        verifyNoInteractions(candidateProvider);
    }

    private Person personWithPhoto(Long id, String storageKey) {
        Photo photo = new Photo.Builder()
                .withId(id * 10)
                .withStorageKey(storageKey)
                .withStatus(PhotoStatus.APPROVED)
                .build();
        return new Person.Builder()
                .withId(id)
                .withPhotos(List.of(photo))
                .build();
    }
}
