// src/main/java/com/saymyname/service/quiz/plugins/OrderingPlugin.java
package com.saymyname.service.quiz.plugins;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.ResultAttribute;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizOrderingRule;
import com.saymyname.core.model.enums.quiz.QuizPayloadType;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizPayloadItem;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionDisplay;
import com.saymyname.core.model.quiz.QuizQuestionPayload;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import com.saymyname.core.model.quiz.QuizValidationResult;
import com.saymyname.core.model.quiz.answer.NormalizedOrdering;
import com.saymyname.core.model.quiz.answer.NormalizedAudit;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.service.quiz.QuizSnapshotGuards;

@Component
public class OrderingPlugin implements QuizQuestionPlugin {

        private static final int DEFAULT_ITEMS = 6;

        @Override
        public QuizFormat supports() {
                return QuizFormat.ORDERING;
        }

        @Override
        public QuizQuestion build(QuizQuestionSpec spec) {

                List<QuizPayloadItem> items = PluginSupport.poolItemsLimited(spec, DEFAULT_ITEMS, null);

                QuizQuestionPayload payload = QuizQuestionPayload.builder()
                                .type(QuizPayloadType.ORDERING)
                                .items(items)
                                .orderBy(QuizOrderingRule.NONE)
                                .build();

                QuizQuestionDisplay display = QuizQuestionDisplay.builder()
                                .prompt("Remets dans le bon ordre")
                                .subtitle(null)
                                .inputPlaceholder(null)
                                .build();

                return PluginSupport.baseQuestion(
                                spec,
                                QuizFormat.ORDERING,
                                payload,
                                null,
                                display,
                                null);
        }

        @Override
        public NormalizedAudit normalize(QuizQuestion question, QuizAnswerSubmission submission) {
                if (submission == null)
                        return new NormalizedOrdering(null);
                return new NormalizedOrdering(submission.getOrderingIds());
        }

        @Override
        public QuizValidationResult validate(QuizQuestionSnapshot snapshot, QuizAnswerSubmission submission,
                        NormalizedAudit normalized) {
                Objects.requireNonNull(snapshot, "snapshot");

                NormalizedOrdering no = (normalized instanceof NormalizedOrdering o) ? o : new NormalizedOrdering(null);

                List<String> expected = snapshot.getTruth() == null ? List.of()
                                : snapshot.getTruth().getCorrectItemKeysInOrder();
                List<String> user = no.orderingIds() == null ? List.of()
                                : no.orderingIds().stream().filter(Objects::nonNull).map(String::valueOf).toList();

                boolean correct = expected.equals(user);
                String correctDisplay = snapshot.getTruth() == null ? null
                                : snapshot.getTruth().getCorrectAnswerDisplay();

                Long attrId = QuizSnapshotGuards.requireSingleTargetAttributeId(snapshot, QuizFormat.ORDERING);

                List<ResultAttribute> attrs = List.of(
                                PluginSupport.resultAttr(attrId, no.auditString(), correct, true));

                return QuizValidationResult.builder()
                                .correct(correct)
                                .correctAnswerDisplay(correctDisplay)
                                .resultAttributes(attrs)
                                .build();
        }
}
