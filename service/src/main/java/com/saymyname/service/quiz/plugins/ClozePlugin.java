// src/main/java/com/saymyname/service/quiz/plugins/ClozePlugin.java
package com.saymyname.service.quiz.plugins;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPayloadType;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionDisplay;
import com.saymyname.core.model.quiz.QuizQuestionPayload;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import com.saymyname.core.model.quiz.QuizValidationResult;
import com.saymyname.core.model.quiz.answer.NormalizedSubmission;
import com.saymyname.core.model.quiz.answer.NormalizedText;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.service.quiz.AnswerKeyService;

@Component
public class ClozePlugin implements QuizQuestionPlugin {

    private final AnswerKeyService answerKeyService;

    public ClozePlugin(AnswerKeyService answerKeyService) {
        this.answerKeyService = Objects.requireNonNull(answerKeyService);
    }

    @Override
    public QuizFormat supports() {
        return QuizFormat.CLOZE;
    }

    @Override
    public QuizQuestion build(QuizQuestionSpec spec) {
        var key = answerKeyService.compute(spec.getPersonId(), spec.getTargetAttributeIds(), spec.getOperator());
        String mask = PluginSupport.maskifyAlphaNum(key.correctAnswerJoined());

        QuizQuestionDisplay display = new QuizQuestionDisplay.Builder()
                .withPrompt("Complète les trous")
                .withSubtitle(null)
                .withInputPlaceholder(null)
                .build();

        QuizQuestionPayload payload = new QuizQuestionPayload.Builder()
                .withType(QuizPayloadType.CLOZE)
                .withMask(mask)
                .build();

        return PluginSupport.baseQuestion(
                spec,
                QuizFormat.CLOZE,
                payload,
                null,
                display,
                null);
    }

    @Override
    public NormalizedSubmission normalize(QuizQuestion question, QuizAnswerSubmission submission) {
        String raw = submission == null ? null : submission.getUserAnswer();
        String canon = PluginSupport.canonicalizeText(raw);
        return new NormalizedText(raw, canon);
    }

    @Override
    public QuizValidationResult validate(QuizQuestionSnapshot snapshot, QuizAnswerSubmission submission,
            NormalizedSubmission normalized) {
        // même logique que TEXT_INPUT
        return new TextInputPlugin().validate(snapshot, submission, normalized);
    }
}
