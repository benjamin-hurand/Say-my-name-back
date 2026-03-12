// src/main/java/com/saymyname/service/quiz/plugins/ClozePlugin.java
package com.saymyname.service.quiz.plugins;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.TargetAnswerResult;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPayloadType;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionDisplay;
import com.saymyname.core.model.quiz.QuizQuestionPayload;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import com.saymyname.core.model.quiz.QuizValidationResult;
import com.saymyname.core.model.quiz.answer.NormalizedAudit;
import com.saymyname.core.model.quiz.answer.NormalizedText;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.service.quiz.AnswerKeyService;
import com.saymyname.service.quiz.QuizSnapshotGuards;

@Component
public class ClozePlugin implements QuizQuestionPlugin {

        private final AnswerKeyService answerKeyService;

        public ClozePlugin(AnswerKeyService answerKeyService) {
                this.answerKeyService = Objects.requireNonNull(answerKeyService, "answerKeyService");
        }

        @Override
        public QuizFormat supports() {
                return QuizFormat.CLOZE;
        }

        @Override
        public QuizQuestion build(QuizQuestionSpec spec) {
                Objects.requireNonNull(spec, "spec");

                String value = answerKeyService.compute(spec.getPersonId(), spec.getTargetAttributeId());

                // Seed déterministe => même question => même masque
                // (questionRound est toujours défini dans ton flow Course)
                long seed = PluginSupport.deterministicSeedFromSpec(spec);

                // Cloze utile: révèle quelques caractères (au minimum 1, souvent 2)
                // Ex: "Abarna" => "A____a" plutôt que "______"
                String mask = PluginSupport.clozeMaskRevealSome(value, seed);

                QuizQuestionDisplay display = new QuizQuestionDisplay.Builder()
                                .withPrompt("Complète les trous")
                                .withSubtitle(null)
                                .withInputPlaceholder("Ta réponse")
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

                NormalizedText nt = (normalized instanceof NormalizedText t)
                                ? t
                                : new NormalizedText(submission == null ? null : submission.getUserAnswer(), null);

                String expected = (snapshot.getTruth() == null) ? null : snapshot.getTruth().getCorrectAnswerDisplay();
                boolean correct = PluginSupport.equalsCanon(nt.raw(), expected)
                                || PluginSupport.equalsCanon(nt.auditString(), expected);

                // CLOZE = single target attribute (comme TEXT_INPUT/CLOZE)
                Long attrId = QuizSnapshotGuards.requireTargetAttributeId(snapshot, QuizFormat.CLOZE);

                TargetAnswerResult result = PluginSupport.result(
                                attrId,
                                nt.raw(),
                                correct,
                                true);

                return new QuizValidationResult.Builder()
                                .withCorrect(correct)
                                .withCorrectAnswerDisplay(expected)
                                .withTargetAnswerResult(result)
                                .build();
        }
}
