// src/main/java/com/saymyname/service/quiz/plugins/WordPuzzlePlugin.java
package com.saymyname.service.quiz.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.quiz.LetterFeedback;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPayloadType;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionDisplay;
import com.saymyname.core.model.quiz.QuizQuestionPayload;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import com.saymyname.core.model.quiz.QuizValidationResult;
import com.saymyname.core.model.quiz.answer.NormalizedAudit;
import com.saymyname.core.model.quiz.answer.NormalizedWordPuzzle;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.core.model.quiz.snapshot.WordPuzzleRules;
import com.saymyname.core.model.quiz.snapshot.WordPuzzleSnapshotState;
import com.saymyname.service.quiz.AnswerKeyService;

@Component
public class WordPuzzlePlugin implements QuizQuestionPlugin {

    private static final int DEFAULT_MAX_ATTEMPTS = 6;

    private final AnswerKeyService answerKeyService;

    public WordPuzzlePlugin(AnswerKeyService answerKeyService) {
        this.answerKeyService = Objects.requireNonNull(answerKeyService);
    }

    @Override
    public QuizFormat supports() {
        return QuizFormat.WORD_PUZZLE;
    }

    @Override
    public QuizQuestion build(QuizQuestionSpec spec) {

        String solution = answerKeyService.compute(spec.getPersonId(), spec.getTargetAttributeId());

        // Note: Using maxErrorsOverride as proxy for maxAttempts until QuizQuestionSpec
        // is extended
        int maxAttempts = spec.getMaxErrorsOverride() != null
                ? Math.max(1, spec.getMaxErrorsOverride())
                : DEFAULT_MAX_ATTEMPTS;

        QuizQuestionDisplay display = new QuizQuestionDisplay.Builder()
                .withPrompt("Trouve le mot en " + maxAttempts + " essais")
                .withSubtitle("Chaque lettre te donne un indice")
                .withInputPlaceholder("Tape un mot de " + solution.length() + " lettres")
                .build();

        QuizQuestionPayload payload = new QuizQuestionPayload.Builder()
                .withType(QuizPayloadType.WORD_PUZZLE)
                .withWordLength(solution.length())
                .withMaxAttempts(maxAttempts)
                .build();

        return PluginSupport.baseQuestion(
                spec,
                QuizFormat.WORD_PUZZLE,
                payload,
                null,
                display,
                null);
    }

    @Override
    public NormalizedAudit normalize(QuizQuestion question, QuizAnswerSubmission submission) {
        if (submission == null || submission.getWordPuzzle() == null) {
            return new NormalizedWordPuzzle(null);
        }

        String word = PluginSupport.canonicalizeText(submission.getWordPuzzle().getWord());
        return new NormalizedWordPuzzle(word != null ? word.toUpperCase() : null);
    }

    @Override
    public QuizValidationResult validate(QuizQuestionSnapshot snapshot, QuizAnswerSubmission submission,
            NormalizedAudit normalized) {

        WordPuzzleSnapshotState currentState = snapshot.getWordPuzzleState();
        WordPuzzleRules rules = snapshot.getWordPuzzleRules();
        String solution = PluginSupport.canonicalizeText(
                snapshot.getTruth().getTargetAttributeValues().get(0).getValue());

        if (solution == null) {
            return buildErrorResult("Invalid solution", currentState, "");
        }

        solution = solution.toUpperCase();
        NormalizedWordPuzzle nw = (NormalizedWordPuzzle) normalized;
        String guess = nw.word();

        // Clone state for mutation
        WordPuzzleSnapshotState newState = cloneState(currentState);

        // Validation: word length
        if (guess == null || guess.length() != solution.length()) {
            return buildErrorResult("Word must be " + solution.length() + " letters", newState, solution);
        }

        // Check if already completed
        if (Boolean.TRUE.equals(currentState.getSolved()) || Boolean.TRUE.equals(currentState.getFailed())) {
            return buildErrorResult("Question already completed", newState, solution);
        }

        // Compute feedback
        List<LetterFeedback> feedback = computeFeedback(guess, solution);

        // Add attempt
        WordPuzzleSnapshotState.AttemptEntry attempt = new WordPuzzleSnapshotState.AttemptEntry.Builder()
                .withWord(guess)
                .withFeedback(feedback)
                .withPosition(currentState.getAttempts().size())
                .build();

        newState.getAttempts().add(attempt);
        newState.setAttemptsRemaining(rules.getMaxAttempts() - newState.getAttempts().size());

        boolean correct = false;

        if (guess.equals(solution)) {
            correct = true;
            newState.setSolved(true);
        } else if (newState.getAttemptsRemaining() == 0) {
            newState.setFailed(true);
        }

        Boolean isComplete = Boolean.TRUE.equals(newState.getSolved()) || Boolean.TRUE.equals(newState.getFailed());

        return new QuizValidationResult.Builder()
                .withCorrect(correct)
                .withCorrectAnswerDisplay(solution)
                .withUpdatedState(newState)
                .withIsComplete(isComplete)
                .withErrorMessage(null)
                .build();
    }

    /**
     * Computes Wordle-style feedback for a guess.
     * Algorithm:
     * 1. Mark EXACT first (green)
     * 2. Count remaining occurrences
     * 3. Mark PRESENT for remaining (yellow)
     * 4. Mark ABSENT for others (gray)
     */
    private List<LetterFeedback> computeFeedback(String guess, String solution) {
        List<LetterFeedback> feedback = new ArrayList<>(guess.length());

        // Track remaining occurrences
        Map<Character, Integer> remainingCounts = new HashMap<>();
        for (char c : solution.toCharArray()) {
            remainingCounts.merge(c, 1, Integer::sum);
        }

        // PHASE 1: Mark EXACT
        boolean[] exactMarked = new boolean[guess.length()];
        for (int i = 0; i < guess.length(); i++) {
            feedback.add(null);
            if (guess.charAt(i) == solution.charAt(i)) {
                feedback.set(i, LetterFeedback.EXACT);
                exactMarked[i] = true;
                remainingCounts.merge(guess.charAt(i), -1, Integer::sum);
            }
        }

        // PHASE 2: Mark PRESENT for remaining occurrences
        for (int i = 0; i < guess.length(); i++) {
            if (exactMarked[i])
                continue;

            char c = guess.charAt(i);
            int remaining = remainingCounts.getOrDefault(c, 0);

            if (remaining > 0) {
                feedback.set(i, LetterFeedback.PRESENT);
                remainingCounts.merge(c, -1, Integer::sum);
            } else {
                feedback.set(i, LetterFeedback.ABSENT);
            }
        }

        return feedback;
    }

    /**
     * Clones the current state for mutation.
     */
    private WordPuzzleSnapshotState cloneState(WordPuzzleSnapshotState current) {
        return new WordPuzzleSnapshotState.Builder()
                .withAttempts(new ArrayList<>(current.getAttempts()))
                .withAttemptsRemaining(current.getAttemptsRemaining())
                .withSolved(current.getSolved())
                .withFailed(current.getFailed())
                .build();
    }

    /**
     * Builds error result.
     */
    private QuizValidationResult buildErrorResult(String error, WordPuzzleSnapshotState state, String correctDisplay) {
        return new QuizValidationResult.Builder()
                .withCorrect(false)
                .withCorrectAnswerDisplay(correctDisplay)
                .withUpdatedState(state)
                .withIsComplete(false)
                .withErrorMessage(error)
                .build();
    }
}
