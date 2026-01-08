// src/main/java/com/saymyname/service/quiz/plugins/HangmanPlugin.java
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
public class HangmanPlugin implements QuizQuestionPlugin {

    private static final int DEFAULT_MAX_ERRORS = 6;

    private final AnswerKeyService answerKeyService;

    public HangmanPlugin(AnswerKeyService answerKeyService) {
        this.answerKeyService = Objects.requireNonNull(answerKeyService);
    }

    @Override
    public QuizFormat supports() {
        return QuizFormat.HANGMAN;
    }

    @Override
    public QuizQuestion build(QuizQuestionSpec spec) {

        var key = answerKeyService.compute(spec.getPersonId(), spec.getTargetAttributeIds(), spec.getOperator());
        String mask = PluginSupport.maskifyAlphaNum(key.correctAnswerJoined());

        int maxErrors = spec.getMaxErrorsOverride() != null
                ? Math.max(1, spec.getMaxErrorsOverride())
                : DEFAULT_MAX_ERRORS;

        QuizQuestionDisplay display = new QuizQuestionDisplay.Builder()
                .withPrompt("Trouve le prénom lettre par lettre")
                .withSubtitle("Fais des erreurs, mais pas trop")
                .withInputPlaceholder(null)
                .build();

        QuizQuestionPayload payload = new QuizQuestionPayload.Builder()
                .withType(QuizPayloadType.HANGMAN)
                .withMask(mask)
                .withMaxErrors(maxErrors)
                .build();

        return PluginSupport.baseQuestion(
                spec,
                QuizFormat.HANGMAN,
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
        return new TextInputPlugin().validate(snapshot, submission, normalized);
    }
}
