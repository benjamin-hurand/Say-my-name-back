// src/main/java/com/saymyname/service/quiz/plugins/McqPlugin.java
package com.saymyname.service.quiz.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

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
import com.saymyname.core.model.quiz.answer.NormalizedChoice;
import com.saymyname.core.model.quiz.answer.NormalizedSubmission;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.service.quiz.AnswerKeyService;

@Component
public class McqPlugin implements QuizQuestionPlugin {

    private static final int DEFAULT_CHOICES = 4;
    private static final int DEFAULT_POOL_SCAN = 30;

    private final AnswerKeyService answerKeyService;
    private final AtomicLong seq = new AtomicLong(1L);

    public McqPlugin(AnswerKeyService answerKeyService) {
        this.answerKeyService = Objects.requireNonNull(answerKeyService, "answerKeyService");
    }

    @Override
    public QuizFormat supports() {
        return QuizFormat.MCQ;
    }

    @Override
    public QuizQuestion build(QuizQuestionSpec spec) {
        Objects.requireNonNull(spec, "spec");

        String correctValue = answerKeyService
                .compute(spec.getPersonId(), spec.getTargetAttributeIds(), spec.getOperator())
                .correctAnswerJoined();

        if (correctValue == null)
            correctValue = "";

        List<String> distractors = new ArrayList<>();
        List<Long> poolIds = PluginSupport.poolIdsLimited(spec, DEFAULT_POOL_SCAN, spec.getPersonId());

        for (Long pid : poolIds) {
            if (pid == null)
                continue;

            String v = answerKeyService
                    .compute(pid, spec.getTargetAttributeIds(), spec.getOperator())
                    .correctAnswerJoined();

            if (v == null || v.isBlank())
                continue;
            if (v.equalsIgnoreCase(correctValue))
                continue;
            boolean already = distractors.stream().anyMatch(d -> d.equalsIgnoreCase(v));
            if (already)
                continue;

            distractors.add(v);
            if (distractors.size() >= DEFAULT_CHOICES - 1)
                break;
        }

        List<QuizChoice> choices = new ArrayList<>(DEFAULT_CHOICES);

        choices.add(new QuizChoice.Builder()
                .withId(nextChoiceId())
                .withLabel(correctValue)
                .withValue(correctValue)
                .withCorrect(true)
                .withPersonId(spec.getPersonId())
                .build());

        for (String d : distractors) {
            choices.add(new QuizChoice.Builder()
                    .withId(nextChoiceId())
                    .withLabel(d)
                    .withValue(d)
                    .withCorrect(false)
                    .withPersonId(null)
                    .build());
        }

        while (choices.size() < DEFAULT_CHOICES) {
            choices.add(new QuizChoice.Builder()
                    .withId(nextChoiceId())
                    .withLabel("")
                    .withValue("")
                    .withCorrect(false)
                    .withPersonId(null)
                    .build());
        }

        QuizQuestionDisplay display = new QuizQuestionDisplay.Builder()
                .withPrompt("Choisis la bonne reponse")
                .withSubtitle(null)
                .withInputPlaceholder(null)
                .build();

        QuizQuestionPayload payload = new QuizQuestionPayload.Builder()
                .withType(QuizPayloadType.MCQ)
                .withChoices(choices)
                .withAllowMultiple(false)
                .build();

        return PluginSupport.baseQuestion(
                spec,
                QuizFormat.MCQ,
                payload,
                null,
                display,
                null);
    }

    @Override
    public NormalizedSubmission normalize(QuizQuestion question, QuizAnswerSubmission submission) {
        if (submission == null) {
            return new NormalizedChoice(null, null);
        }

        Long selectedId = submission.getSelectedChoiceId();
        String selectedValue = null;

        if (selectedId != null && question != null && question.getPayload() != null) {
            List<QuizChoice> choices = question.getPayload().getChoices();
            if (choices != null) {
                for (QuizChoice c : choices) {
                    if (c != null && Objects.equals(selectedId, c.getId())) {
                        selectedValue = c.getValue();
                        break;
                    }
                }
            }
        }

        if (selectedValue == null) {
            String ua = submission.getUserAnswer();
            selectedValue = ua == null ? null : ua.trim();
        }

        return new NormalizedChoice(selectedId, selectedValue);
    }

    @Override
    public QuizValidationResult validate(
            QuizQuestionSnapshot snapshot,
            QuizAnswerSubmission submission,
            NormalizedSubmission normalized) {

        Objects.requireNonNull(snapshot, "snapshot");

        NormalizedChoice nc = (normalized instanceof NormalizedChoice c) ? c : new NormalizedChoice(null, null);

        List<String> correctKeys = snapshot.getTruth() == null ? List.of() : snapshot.getTruth().getCorrectChoiceKeys();

        boolean correct = false;
        if (nc.selectedChoiceId() != null && correctKeys != null && !correctKeys.isEmpty()) {
            String key = String.valueOf(nc.selectedChoiceId());
            correct = correctKeys.contains(key);
        }
        if (!correct && nc.selectedValue() != null && snapshot.getTruth() != null) {
            String expected = snapshot.getTruth().getCorrectAnswerDisplay();
            correct = PluginSupport.equalsCanon(nc.selectedValue(), expected);
        }

        Long attrId = (snapshot.getTargetAttributeIds() == null || snapshot.getTargetAttributeIds().isEmpty())
                ? null
                : snapshot.getTargetAttributeIds().get(0);

        List<ResultAttribute> attrs = List.of(
                PluginSupport.resultAttr(
                        attrId,
                        nc.selectedValue(),
                        correct,
                        true));

        return new QuizValidationResult.Builder()
                .withCorrect(correct)
                .withCorrectAnswerDisplay(
                        snapshot.getTruth() == null ? null : snapshot.getTruth().getCorrectAnswerDisplay())
                .withResultAttributes(attrs)
                .build();
    }

    private Long nextChoiceId() {
        return seq.getAndIncrement();
    }
}
