// src/main/java/com/saymyname/service/quiz/plugins/OrderingPlugin.java
package com.saymyname.service.quiz.plugins;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
import com.saymyname.core.model.quiz.answer.NormalizedSubmission;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;

@Component
public class OrderingPlugin implements QuizQuestionPlugin {

    private static final int DEFAULT_ITEMS = 6;

    @Override
    public QuizFormat supports() {
        return QuizFormat.ORDERING;
    }

    @Override
    public QuizQuestion build(QuizQuestionSpec spec) {

        List<Long> poolIds = PluginSupport.poolIdsLimited(spec, DEFAULT_ITEMS, null);

        List<QuizPayloadItem> items = poolIds.stream()
                .map(id -> new QuizPayloadItem.Builder()
                        .withPersonId(id)
                        .withStorageKey(null)
                        .withLabelId(String.valueOf(id))
                        .build())
                .collect(Collectors.toList());

        QuizQuestionPayload payload = new QuizQuestionPayload.Builder()
                .withType(QuizPayloadType.ORDERING)
                .withItems(items)
                .withOrderBy(QuizOrderingRule.NONE)
                .build();

        QuizQuestionDisplay display = new QuizQuestionDisplay.Builder()
                .withPrompt("Remets dans le bon ordre")
                .withSubtitle(null)
                .withInputPlaceholder(null)
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
    public NormalizedSubmission normalize(QuizQuestion question, QuizAnswerSubmission submission) {
        if (submission == null)
            return new NormalizedOrdering(null);
        return new NormalizedOrdering(submission.getOrderingIds());
    }

    @Override
    public QuizValidationResult validate(QuizQuestionSnapshot snapshot, QuizAnswerSubmission submission,
            NormalizedSubmission normalized) {
        Objects.requireNonNull(snapshot, "snapshot");

        NormalizedOrdering no = (normalized instanceof NormalizedOrdering o) ? o : new NormalizedOrdering(null);

        List<String> expected = snapshot.getTruth() == null ? List.of()
                : snapshot.getTruth().getCorrectItemKeysInOrder();
        List<String> user = no.orderingIds() == null ? List.of()
                : no.orderingIds().stream().filter(Objects::nonNull).map(String::valueOf).toList();

        boolean correct = expected.equals(user);
        String correctDisplay = snapshot.getTruth() == null ? null : snapshot.getTruth().getCorrectAnswerDisplay();

        Long attrId = (snapshot.getTargetAttributeIds() == null || snapshot.getTargetAttributeIds().isEmpty())
                ? null
                : snapshot.getTargetAttributeIds().get(0);

        List<ResultAttribute> attrs = List.of(
                PluginSupport.resultAttr(attrId, no.auditString(), correct, true));

        return new QuizValidationResult.Builder()
                .withCorrect(correct)
                .withCorrectAnswerDisplay(correctDisplay)
                .withResultAttributes(attrs)
                .build();
    }
}
