// src/main/java/com/saymyname/service/quiz/plugins/TextInputPlugin.java
package com.saymyname.service.quiz.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.ResultAttribute;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPayloadType;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionDisplay;
import com.saymyname.core.model.quiz.QuizQuestionHints;
import com.saymyname.core.model.quiz.QuizQuestionPayload;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import com.saymyname.core.model.quiz.QuizValidationResult;
import com.saymyname.core.model.quiz.answer.NormalizedAudit;
import com.saymyname.core.model.quiz.answer.NormalizedText;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.core.model.quiz.snapshot.TruthAttributeValue;

@Component
public class TextInputPlugin implements QuizQuestionPlugin {

    @Override
    public QuizFormat supports() {
        return QuizFormat.TEXT_INPUT;
    }

    @Override
    public QuizQuestion build(QuizQuestionSpec spec) {

        QuizQuestionHints hints = PluginSupport.hintsFromSpec(spec);

        QuizQuestionPayload payload = new QuizQuestionPayload.Builder()
                .withType(QuizPayloadType.TEXT_INPUT)
                .build();

        QuizQuestionDisplay display = new QuizQuestionDisplay.Builder()
                .withPrompt("Ecris la bonne reponse")
                .withSubtitle(null)
                .withInputPlaceholder("Tape ici")
                .build();

        return PluginSupport.baseQuestion(
                spec,
                QuizFormat.TEXT_INPUT,
                payload,
                hints,
                display,
                null);
    }

    @Override
    public NormalizedAudit normalize(QuizQuestion question, QuizAnswerSubmission submission) {
        String raw = submission == null ? null : submission.getUserAnswer();
        String canon = PluginSupport.canonicalizeText(raw);
        return new NormalizedText(raw, canon);
    }

    @Override
    public QuizValidationResult validate(
            QuizQuestionSnapshot snapshot,
            QuizAnswerSubmission submission,
            NormalizedAudit normalized) {

        Objects.requireNonNull(snapshot, "snapshot");

        NormalizedText nt = (normalized instanceof NormalizedText t) ? t : new NormalizedText(null, null);
        String userCanon = nt.canonical();

        List<TruthAttributeValue> truths = snapshot.getTruth() == null ? List.of()
                : snapshot.getTruth().getTargetAttributeValues();

        boolean correct = false;
        String correctDisplay = snapshot.getTruth() == null ? null : snapshot.getTruth().getCorrectAnswerDisplay();

        if (userCanon != null && truths != null && !truths.isEmpty()) {
            for (TruthAttributeValue tav : truths) {
                if (tav == null)
                    continue;
                if (PluginSupport.equalsCanon(userCanon, tav.getValue())) {
                    correct = true;
                    break;
                }
            }
        }

        List<ResultAttribute> attrs = new ArrayList<>();
        if (snapshot.getTargetAttributeIds() != null) {
            for (Long attrId : snapshot.getTargetAttributeIds()) {
                if (attrId == null)
                    continue;
                attrs.add(PluginSupport.resultAttr(
                        attrId,
                        nt.raw(),
                        correct,
                        true));
            }
        }

        return new QuizValidationResult.Builder()
                .withCorrect(correct)
                .withCorrectAnswerDisplay(correctDisplay)
                .withResultAttributes(attrs)
                .build();
    }
}
