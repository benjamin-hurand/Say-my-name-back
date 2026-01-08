// src/main/java/com/saymyname/service/course/CourseQuizQuestionBuilder.java
package com.saymyname.service.course;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionHistory;
import com.saymyname.core.model.course.CourseQuestionPlan;
import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.core.model.enums.course.CourseQuestionItemRole;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionContext;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import com.saymyname.core.model.quiz.options.GameOptions;
import com.saymyname.service.quiz.CourseToOptionsAdapter;
import com.saymyname.service.quiz.QuizCandidateProvider;
import com.saymyname.service.quiz.QuizQuestionFactory;

@Component
public class CourseQuizQuestionBuilder {

        private static final int DEFAULT_POOL_SIZE = 30;

        private final QuizCandidateProvider candidateProvider;
        private final QuizQuestionFactory questionFactory;

        public CourseQuizQuestionBuilder(
                        QuizCandidateProvider candidateProvider,
                        QuizQuestionFactory questionFactory) {

                this.candidateProvider = Objects.requireNonNull(candidateProvider, "candidateProvider");
                this.questionFactory = Objects.requireNonNull(questionFactory, "questionFactory");
        }

        public QuizQuestion buildFromHistory(
                        CourseQuestionHistory h,
                        CourseQuestionPlan plan) {
                Objects.requireNonNull(h, "history");
                Objects.requireNonNull(plan, "plan");
                Objects.requireNonNull(h.getCourse(), "history.course");
                Objects.requireNonNull(h.getCourse().getUser(), "history.course.user");
                Objects.requireNonNull(h.getCourse().getGameMode(), "history.course.gameMode");

                Course course = h.getCourse();

                // ✅ Dans ton modèle refacto, le "target" est dans items[role=TARGET]
                List<Person> targetPersons = h.getItems().stream()
                                .filter(it -> it.getRole() != null && it.getRole() == CourseQuestionItemRole.TARGET)
                                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                                .map(it -> it.getPerson())
                                .filter(Objects::nonNull)
                                .toList();

                Person targetPerson = targetPersons.stream()
                                .findFirst()
                                .orElseThrow(
                                                () -> new IllegalStateException("CourseQuestionHistory " + h.getId()
                                                                + " has no TARGET item"));

                String storageKey = approvedStorageKeyOrThrow(targetPerson);

                GameOptions options = CourseToOptionsAdapter.toGameOptions(course);

                List<Person> pool = candidateProvider.candidates(options, course.getUser().getId(), DEFAULT_POOL_SIZE);
                List<Long> poolIds = pool.stream()
                                .map(Person::getId)
                                .filter(Objects::nonNull)
                                .toList();

                List<Long> targetPersonIds = targetPersons.stream()
                                .map(Person::getId)
                                .filter(Objects::nonNull)
                                .toList();

                List<Long> candidatePoolIds = (plan.getFormat() == QuizFormat.ORDERING
                                || plan.getFormat() == QuizFormat.ASSOCIATION)
                                                ? targetPersonIds
                                                : poolIds;

                final List<Long> targetAttributeIds = options.getGameMode().getGameModeAttributes().stream()
                                .map(gma -> gma.getAttribute().getId())
                                .toList();

                final String operator = options.getGameMode().getOperator();

                QuizQuestionContext ctx = new QuizQuestionContext.Builder()
                                .withSource(QuizQuestionSource.COURSE)
                                .withCourseId(course.getId())
                                .withCourseQuestionId(h.getId())
                                .withQuestionRound(h.getQuestionRound())
                                .withPoolType(h.getPoolType())
                                .build();

                QuizQuestionSpec.Builder specBuilder = new QuizQuestionSpec.Builder()
                                .withSource(QuizQuestionSource.COURSE)
                                .withPersonId(targetPerson.getId())
                                .withStorageKey(storageKey)
                                .withGameModeId(course.getGameMode().getId())
                                .withTargetAttributeIds(targetAttributeIds)
                                .withOperator(operator)
                                .withContext(ctx)
                                .withInitials(null)
                                .withCandidatePoolPersonIds(candidatePoolIds)
                                .withTimed(plan.isTimed())
                                .withTimeLimitMs(plan.getTimeLimitMs())
                                .withReasonCode(plan.getReasonCode())
                                .withReasonDetailsJson(plan.getReasonDetailsJson());

                QuizQuestionSpec spec = specBuilder.build();

                return questionFactory.pluginFor(plan.getFormat()).build(spec);
        }

        private static String approvedStorageKeyOrThrow(Person person) {
                return person.getPhotos().stream()
                                .filter(p -> p.getStatus() == PhotoStatus.APPROVED)
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException(
                                                "Person " + person.getId()
                                                                + " n'a pas de photo APPROVED malgré le filtre SQL"))
                                .getStorageKey();
        }
}
