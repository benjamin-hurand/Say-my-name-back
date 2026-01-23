// src/main/java/com/saymyname/service/course/CourseQuizQuestionBuilder.java
package com.saymyname.service.course;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionAttempt;
import com.saymyname.core.model.course.CourseQuestionItem;
import com.saymyname.core.model.course.CourseQuestionPlan;
import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.core.model.enums.course.QuizQuestionItemRole;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionContext;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import com.saymyname.core.model.quiz.QuizPayloadItem;
import com.saymyname.core.model.quiz.options.GameOptions;
import com.saymyname.service.quiz.CourseOptionsResolver;
import com.saymyname.service.quiz.QuizCandidateProvider;
import com.saymyname.service.quiz.QuizQuestionFactory;

@Component
public class CourseQuizQuestionBuilder {

        private static final Logger log = LoggerFactory.getLogger(CourseQuizQuestionBuilder.class);

        private static final int DEFAULT_POOL_SIZE = 30;
        private static final int DEFAULT_MCQ_DISTRACTORS = 3;

        private final QuizCandidateProvider candidateProvider;
        private final QuizQuestionFactory questionFactory;
        private final CourseOptionsResolver courseOptionsResolver;

        public CourseQuizQuestionBuilder(
                        QuizCandidateProvider candidateProvider,
                        QuizQuestionFactory questionFactory,
                        CourseOptionsResolver courseOptionsResolver) {

                this.candidateProvider = Objects.requireNonNull(candidateProvider, "candidateProvider");
                this.questionFactory = Objects.requireNonNull(questionFactory, "questionFactory");
                this.courseOptionsResolver = Objects.requireNonNull(courseOptionsResolver, "courseOptionsResolver");
        }

        public QuizQuestion buildFromAttempt(
                        CourseQuestionAttempt h,
                        CourseQuestionPlan plan) {
                Objects.requireNonNull(h, "attempt");
                Objects.requireNonNull(plan, "plan");
                Objects.requireNonNull(h.getCourse(), "attempt.course");
                Objects.requireNonNull(h.getCourse().getUser(), "attempt.course.user");
                Objects.requireNonNull(h.getCourse().getGameMode(), "attempt.course.gameMode");

                Course course = h.getCourse();

                // Targets are stored in items with role TARGET.
                List<Person> targetPersons = h.getItems().stream()
                                .filter(it -> it.getRole() != null && it.getRole() == QuizQuestionItemRole.TARGET)
                                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                                .map(CourseQuestionItem::getPerson)
                                .filter(Objects::nonNull)
                                .toList();

                Person targetPerson = targetPersons.stream()
                                .findFirst()
                                .orElseThrow(
                                                () -> new IllegalStateException("CourseQuestionAttempt " + h.getId()
                                                                + " has no TARGET item"));

                String storageKey = approvedStorageKeyOrThrow(targetPerson);

                GameOptions options = courseOptionsResolver.resolve(course);

                List<Person> distractorPersons = h.getItems().stream()
                                .filter(it -> it.getRole() == QuizQuestionItemRole.DISTRACTOR)
                                .map(CourseQuestionItem::getPerson)
                                .filter(Objects::nonNull)
                                .toList();

                List<Long> targetPersonIds = targetPersons.stream()
                                .map(Person::getId)
                                .filter(Objects::nonNull)
                                .toList();

                List<QuizPayloadItem> targetItems = toCandidateItems(targetPersons);

                QuizFormat format = plan.getFormat();
                String reasonDetailsJson = plan.getReasonDetailsJson();

                if (format == QuizFormat.MCQ && distractorPersons.isEmpty()) {
                        QuizFormat fallbackFormat = resolveNonMcqFallbackFormat();
                        if (fallbackFormat != null) {
                                log.warn("MCQ plan missing distractors for courseQuestionId={}, fallbackFormat={}",
                                                h.getId(), fallbackFormat);
                                format = fallbackFormat;
                                plan.setFormat(fallbackFormat);
                                plan.setParamsJson(null);
                                reasonDetailsJson = buildFallbackReasonDetails(
                                                reasonDetailsJson,
                                                "replan_format",
                                                fallbackFormat);
                                plan.setReasonDetailsJson(reasonDetailsJson);
                        } else {
                                log.warn("MCQ plan missing distractors for courseQuestionId={}, using candidateProvider",
                                                h.getId());
                                distractorPersons = candidateProvider.distractors(
                                                options,
                                                course.getUser().getId(),
                                                targetPerson.getId(),
                                                DEFAULT_MCQ_DISTRACTORS);
                                if (distractorPersons == null) {
                                        distractorPersons = List.of();
                                }
                                reasonDetailsJson = buildFallbackReasonDetails(
                                                reasonDetailsJson,
                                                "candidate_provider",
                                                null);
                                plan.setReasonDetailsJson(reasonDetailsJson);
                        }
                }

                boolean multiTarget = format == QuizFormat.ORDERING
                                || format == QuizFormat.ASSOCIATION;

                List<Long> candidatePoolIds;
                List<QuizPayloadItem> candidatePoolItems;

                if (multiTarget) {
                        candidatePoolIds = targetPersonIds;
                        candidatePoolItems = targetItems;
                } else if (format == QuizFormat.MCQ) {
                        candidatePoolIds = distractorPersons.stream()
                                        .map(Person::getId)
                                        .filter(Objects::nonNull)
                                        .toList();
                        candidatePoolItems = toCandidateItems(distractorPersons);
                } else {
                        List<Person> pool = candidateProvider.candidates(
                                        options,
                                        course.getUser().getId(),
                                        DEFAULT_POOL_SIZE);
                        candidatePoolIds = pool.stream()
                                        .map(Person::getId)
                                        .filter(Objects::nonNull)
                                        .toList();
                        candidatePoolItems = toCandidateItems(pool);
                }

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
                                .withCandidatePoolItems(candidatePoolItems)
                                .withTimed(plan.isTimed())
                                .withTimeLimitMs(plan.getTimeLimitMs())
                                .withReasonCode(plan.getReasonCode())
                                .withReasonDetailsJson(reasonDetailsJson);

                QuizQuestionSpec spec = specBuilder.build();

                return questionFactory.pluginFor(format).build(spec);
        }

        private QuizFormat resolveNonMcqFallbackFormat() {
                for (QuizFormat candidate : List.of(QuizFormat.CLOZE, QuizFormat.TEXT_INPUT)) {
                        if (questionFactory.supportsFormat(candidate)) {
                                return candidate;
                        }
                }
                return null;
        }

        private static String approvedStorageKeyOrThrow(Person person) {
                return person.getPhotos().stream()
                                .filter(p -> p.getStatus() == PhotoStatus.APPROVED)
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException(
                                                "Person " + person.getId()
                                                                + " has no APPROVED photo despite SQL filter"))
                                .getStorageKey();
        }

        private static List<QuizPayloadItem> toCandidateItems(List<Person> persons) {
                if (persons == null || persons.isEmpty()) {
                        return List.of();
                }
                return persons.stream()
                                .filter(Objects::nonNull)
                                .map(p -> {
                                        Long id = p.getId();
                                        if (id == null) {
                                                return null;
                                        }
                                        String storageKey = approvedStorageKeyOrThrow(p);
                                        return new QuizPayloadItem.Builder()
                                                        .withPersonId(id)
                                                        .withStorageKey(storageKey)
                                                        .withLabelId(String.valueOf(id))
                                                        .build();
                                })
                                .filter(Objects::nonNull)
                                .toList();
        }

        private static String buildFallbackReasonDetails(
                        String baseReasonDetailsJson,
                        String fallbackStrategy,
                        QuizFormat fallbackFormat) {
                return new JsonBuilder()
                                .add("fallback_reason", "mcq_missing_distractors")
                                .add("fallback_strategy", fallbackStrategy)
                                .add("fallback_format", fallbackFormat != null ? fallbackFormat.name() : null)
                                .addRaw("base_reason_details", baseReasonDetailsJson)
                                .build();
        }

        private static final class JsonBuilder {
                private final StringBuilder sb = new StringBuilder(160);
                private boolean first = true;

                private JsonBuilder() {
                        sb.append('{');
                }

                JsonBuilder add(String key, String value) {
                        if (value == null) {
                                return this;
                        }
                        sep();
                        sb.append('"').append(escape(key)).append("\":\"").append(escape(value)).append('"');
                        return this;
                }

                JsonBuilder addRaw(String key, String rawJson) {
                        if (rawJson == null || rawJson.isBlank()) {
                                return this;
                        }
                        sep();
                        sb.append('"').append(escape(key)).append("\":").append(rawJson);
                        return this;
                }

                String build() {
                        sb.append('}');
                        return sb.toString();
                }

                private void sep() {
                        if (!first) {
                                sb.append(',');
                        }
                        first = false;
                }

                private static String escape(String s) {
                        if (s == null) {
                                return null;
                        }
                        StringBuilder out = new StringBuilder(s.length() + 8);
                        for (int i = 0; i < s.length(); i++) {
                                char c = s.charAt(i);
                                if (c == '"' || c == '\\') {
                                        out.append('\\');
                                }
                                out.append(c);
                        }
                        return out.toString();
                }
        }
}
