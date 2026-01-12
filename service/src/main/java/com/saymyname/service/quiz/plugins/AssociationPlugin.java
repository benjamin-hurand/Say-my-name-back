// src/main/java/com/saymyname/service/quiz/plugins/AssociationPlugin.java
package com.saymyname.service.quiz.plugins;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.ResultAttribute;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPayloadType;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizAssociationPair;
import com.saymyname.core.model.quiz.QuizPayloadItem;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionDisplay;
import com.saymyname.core.model.quiz.QuizQuestionPayload;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import com.saymyname.core.model.quiz.QuizValidationResult;
import com.saymyname.core.model.quiz.answer.NormalizedAssociation;
import com.saymyname.core.model.quiz.answer.NormalizedSubmission;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.core.model.quiz.snapshot.TruthPair;
import com.saymyname.service.quiz.QuizSnapshotGuards;

@Component
public class AssociationPlugin implements QuizQuestionPlugin {

    private static final int DEFAULT_ITEMS = 6;

    @Override
    public QuizFormat supports() {
        return QuizFormat.ASSOCIATION;
    }

    @Override
    public QuizQuestion build(QuizQuestionSpec spec) {
        Objects.requireNonNull(spec, "spec");

        List<QuizPayloadItem> items = PluginSupport.poolItemsLimited(spec, DEFAULT_ITEMS, null);

        QuizQuestionPayload payload = new QuizQuestionPayload.Builder()
                .withType(QuizPayloadType.ASSOCIATION)
                .withItems(items)
                .build();

        QuizQuestionDisplay display = new QuizQuestionDisplay.Builder()
                .withPrompt("Associe les bons éléments")
                .withSubtitle(null)
                .withInputPlaceholder(null)
                .build();

        return PluginSupport.baseQuestion(
                spec,
                QuizFormat.ASSOCIATION,
                payload,
                null,
                display,
                null);
    }

    @Override
    public NormalizedSubmission normalize(QuizQuestion question, QuizAnswerSubmission submission) {
        List<QuizAssociationPair> pairs = submission == null ? null : submission.getPairs();
        return new NormalizedAssociation(pairs);
    }

    @Override
    public QuizValidationResult validate(QuizQuestionSnapshot snapshot, QuizAnswerSubmission submission,
            NormalizedSubmission normalized) {
        Objects.requireNonNull(snapshot, "snapshot");

        NormalizedAssociation na = (normalized instanceof NormalizedAssociation a) ? a
                : new NormalizedAssociation(null);

        var expected = snapshot.getTruth() == null ? List.<TruthPair>of() : snapshot.getTruth().getCorrectPairs();

        var userPairs = na.pairs() == null ? List.<TruthPair>of()
                : na.pairs().stream()
                        .filter(Objects::nonNull)
                        .filter(p -> p.getLeftId() != null && p.getRightId() != null)
                        .map(p -> new TruthPair(p.getLeftId(), p.getRightId()))
                        .toList();

        boolean correct = new java.util.HashSet<>(expected).equals(new java.util.HashSet<>(userPairs));
        String correctDisplay = snapshot.getTruth() == null ? null : snapshot.getTruth().getCorrectAnswerDisplay();

        Long attrId = QuizSnapshotGuards.requireSingleTargetAttributeId(snapshot, QuizFormat.ASSOCIATION);

        List<ResultAttribute> attrs = List.of(
                PluginSupport.resultAttr(attrId, na.auditString(), correct, true));

        return new QuizValidationResult.Builder()
                .withCorrect(correct)
                .withCorrectAnswerDisplay(correctDisplay)
                .withResultAttributes(attrs)
                .build();
    }
}
