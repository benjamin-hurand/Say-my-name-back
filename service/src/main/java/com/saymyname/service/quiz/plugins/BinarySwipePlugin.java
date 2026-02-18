// src/main/java/com/saymyname/service/quiz/plugins/BinarySwipePlugin.java
package com.saymyname.service.quiz.plugins;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.ResultAttribute;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPayloadType;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizChoice;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionDisplay;
import com.saymyname.core.model.quiz.QuizQuestionPayload;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import com.saymyname.core.model.quiz.QuizValidationResult;
import com.saymyname.core.model.quiz.answer.NormalizedBinary;
import com.saymyname.core.model.quiz.answer.NormalizedAudit;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.service.quiz.AnswerKeyService;
import com.saymyname.service.quiz.QuizSnapshotGuards;

@Component
public class BinarySwipePlugin implements QuizQuestionPlugin {

    private final AnswerKeyService answerKeyService;

    public BinarySwipePlugin(AnswerKeyService answerKeyService) {
        this.answerKeyService = Objects.requireNonNull(answerKeyService, "answerKeyService");
    }

    @Override
    public QuizFormat supports() {
        return QuizFormat.BINARY_SWIPE;
    }

    @Override
    public QuizQuestion build(QuizQuestionSpec spec) {
        Objects.requireNonNull(spec, "spec");

        String correct = answerKeyService
                .compute(spec.getPersonId(), spec.getTargetAttributeIds(), spec.getOperator())
                .correctAnswerJoined();

        QuizChoice proposition = QuizChoice.builder()
                .id(1L)
                .label("Ce prénom est " + (correct == null ? "…" : correct) + " ?")
                .value(correct == null ? "" : correct)
                .personId(spec.getPersonId())
                .build();

        QuizQuestionPayload payload = QuizQuestionPayload.builder()
                .type(QuizPayloadType.BINARY_SWIPE)
                .proposition(proposition)
                .build();

        QuizQuestionDisplay display = QuizQuestionDisplay.builder()
                .prompt("Vrai ou faux ?")
                .subtitle("Swipe à gauche/droite")
                .inputPlaceholder(null)
                .build();

        return PluginSupport.baseQuestion(
                spec,
                QuizFormat.BINARY_SWIPE,
                payload,
                null,
                display,
                null);
    }

    @Override
    public NormalizedAudit normalize(QuizQuestion question, QuizAnswerSubmission submission) {
        if (submission == null) {
            return new NormalizedBinary(null);
        }

        Boolean sr = submission.getSwipeRight();
        if (sr != null)
            return new NormalizedBinary(sr);

        // fallback texte
        String ua = submission.getUserAnswer();
        if (ua == null)
            return new NormalizedBinary(null);

        String n = PluginSupport.canonicalizeText(ua);
        if (n == null)
            return new NormalizedBinary(null);

        if (n.equals("true") || n.equals("vrai") || n.equals("oui") || n.equals("yes"))
            return new NormalizedBinary(true);
        if (n.equals("false") || n.equals("faux") || n.equals("non") || n.equals("no"))
            return new NormalizedBinary(false);

        return new NormalizedBinary(null);
    }

    @Override
    public QuizValidationResult validate(QuizQuestionSnapshot snapshot, QuizAnswerSubmission submission,
            NormalizedAudit normalized) {
        Objects.requireNonNull(snapshot, "snapshot");

        NormalizedBinary nb = (normalized instanceof NormalizedBinary b) ? b : new NormalizedBinary(null);

        Boolean expected = snapshot.getTruth() == null ? null : snapshot.getTruth().getCorrectSwipeRight();
        boolean correct = expected != null && nb.swipeRight() != null && expected.equals(nb.swipeRight());
        String expectedDisplay = snapshot.getTruth() == null ? null : snapshot.getTruth().getCorrectAnswerDisplay();

        Long attrId = QuizSnapshotGuards.requireSingleTargetAttributeId(snapshot, QuizFormat.BINARY_SWIPE);

        List<ResultAttribute> attrs = List.of(
                PluginSupport.resultAttr(attrId, nb.auditString(), correct, true));

        return QuizValidationResult.builder()
                .correct(correct)
                .correctAnswerDisplay(expectedDisplay)
                .resultAttributes(attrs)
                .build();
    }
}
