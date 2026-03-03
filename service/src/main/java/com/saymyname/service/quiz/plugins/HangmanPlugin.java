// src/main/java/com/saymyname/service/quiz/plugins/HangmanPlugin.java
package com.saymyname.service.quiz.plugins;

import java.util.ArrayList;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.quiz.HangmanAction;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPayloadType;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionDisplay;
import com.saymyname.core.model.quiz.QuizQuestionPayload;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import com.saymyname.core.model.quiz.QuizValidationResult;
import com.saymyname.core.model.quiz.answer.NormalizedHangman;
import com.saymyname.core.model.quiz.answer.NormalizedAudit;
import com.saymyname.core.model.quiz.snapshot.HangmanRules;
import com.saymyname.core.model.quiz.snapshot.HangmanSnapshotState;
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

        String value = answerKeyService.compute(spec.getPersonId(), spec.getTargetAttributeId());
        String mask = PluginSupport.maskifyAlphaNum(value);

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
    public NormalizedAudit normalize(QuizQuestion question, QuizAnswerSubmission submission) {
        if (submission == null || submission.getHangman() == null) {
            return new NormalizedHangman(null, null, null);
        }

        QuizAnswerSubmission.HangmanSubmission h = submission.getHangman();
        HangmanAction action = h.getAction();

        if (action == HangmanAction.GUESS) {
            String letter = PluginSupport.canonicalizeText(h.getLetter());
            return new NormalizedHangman(action, letter != null ? letter.toUpperCase() : null, null);
        } else if (action == HangmanAction.SOLVE) {
            String value = PluginSupport.canonicalizeText(h.getValue());
            return new NormalizedHangman(action, null, value != null ? value.toUpperCase() : null);
        }

        return new NormalizedHangman(null, null, null);
    }

    @Override
    public QuizValidationResult validate(QuizQuestionSnapshot snapshot, QuizAnswerSubmission submission,
            NormalizedAudit normalized) {

        HangmanSnapshotState currentState = snapshot.getHangmanState();
        HangmanRules rules = snapshot.getHangmanRules();
        String solution = PluginSupport.canonicalizeText(
                snapshot.getTruth().getTargetAttributeValues().get(0).getValue());

        if (solution == null) {
            return buildErrorResult("Invalid solution", currentState, "");
        }

        solution = solution.toUpperCase();
        NormalizedHangman nh = (NormalizedHangman) normalized;

        // Clone state for mutation
        HangmanSnapshotState newState = cloneState(currentState);

        boolean correct = false;

        if (nh.action() == HangmanAction.GUESS) {
            String letter = nh.letter();

            // Validation
            if (letter == null || letter.length() != 1) {
                return buildErrorResult("Invalid letter", newState, solution);
            }
            if (currentState.getTriedLetters().contains(letter)) {
                return buildErrorResult("Letter already tried", newState, solution);
            }

            // Update state
            newState.getTriedLetters().add(letter);

            if (solution.contains(letter)) {
                // CORRECT LETTER: Update mask
                newState.setMask(updateMask(currentState.getMask(), solution, letter));

                // Check if solved
                if (!newState.getMask().contains("_")) {
                    correct = true; // SOLVED
                }
            } else {
                // WRONG LETTER
                newState.getWrongLetters().add(letter);
                newState.setErrorsCount(newState.getErrorsCount() + 1);
            }

        } else if (nh.action() == HangmanAction.SOLVE) {
            String value = nh.value();

            if (value == null) {
                return buildErrorResult("Invalid word", newState, solution);
            }

            if (value.equals(solution)) {
                correct = true; // SOLVED
                newState.setMask(solution);
            } else {
                // Wrong solve attempt counts as error
                newState.setErrorsCount(newState.getErrorsCount() + 1);
            }
        } else {
            return buildErrorResult("Invalid action", newState, solution);
        }

        // Determine completion
        boolean isSolved = !newState.getMask().contains("_");
        boolean isFailed = newState.getErrorsCount() >= rules.getMaxErrors();
        Boolean isComplete = (isSolved || isFailed) ? true : false;

        return new QuizValidationResult.Builder()
                .withCorrect(correct)
                .withCorrectAnswerDisplay(solution)
                .withUpdatedState(newState)
                .withIsComplete(isComplete)
                .withErrorMessage(null)
                .build();
    }

    /**
     * Updates mask by revealing all positions matching the guessed letter.
     */
    private String updateMask(String currentMask, String solution, String letter) {
        StringBuilder newMask = new StringBuilder(currentMask);
        for (int i = 0; i < solution.length(); i++) {
            if (solution.substring(i, i + 1).equalsIgnoreCase(letter)) {
                newMask.setCharAt(i * 2, solution.charAt(i)); // Account for spaces in mask
            }
        }
        return newMask.toString();
    }

    /**
     * Clones the current state for mutation.
     */
    private HangmanSnapshotState cloneState(HangmanSnapshotState current) {
        return new HangmanSnapshotState.Builder()
                .withMask(current.getMask())
                .withErrorsCount(current.getErrorsCount())
                .withTriedLetters(new ArrayList<>(current.getTriedLetters()))
                .withWrongLetters(new ArrayList<>(current.getWrongLetters()))
                .build();
    }

    /**
     * Builds error result.
     */
    private QuizValidationResult buildErrorResult(String error, HangmanSnapshotState state, String correctDisplay) {
        return new QuizValidationResult.Builder()
                .withCorrect(false)
                .withCorrectAnswerDisplay(correctDisplay)
                .withUpdatedState(state)
                .withIsComplete(false)
                .withErrorMessage(error)
                .build();
    }
}
