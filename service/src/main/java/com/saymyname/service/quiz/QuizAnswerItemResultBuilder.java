package com.saymyname.service.quiz;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.saymyname.core.model.enums.course.QuizQuestionItemRole;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.QuizAnswerItemResult;
import com.saymyname.core.model.quiz.QuizChoice;
import com.saymyname.core.model.quiz.QuizEvaluationResult;
import com.saymyname.core.model.quiz.answer.NormalizedBinary;
import com.saymyname.core.model.quiz.answer.NormalizedChoice;
import com.saymyname.core.model.quiz.answer.NormalizedText;
import com.saymyname.core.model.quiz.snapshot.HangmanSnapshotState;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.core.model.quiz.snapshot.WordPuzzleSnapshotState;

/**
 * Construit les QuizAnswerItemResult pour affichage des corrections côté UI.
 *
 * SPÉCIFICATIONS PAR FORMAT:
 *
 * TEXT_INPUT/CLOZE:
 * - 1 item
 * - userAnswerNormalized = texte saisi normalisé (NormalizedText.raw())
 * - correctAnswer = texte attendu (correctAnswerDisplay)
 *
 * MCQ (single, allowMultiple=false):
 * - 1 item
 * - userAnswerNormalized = label du choix sélectionné
 * - correctAnswer = label du choix correct
 *
 * MCQ (multiple, allowMultiple=true):
 * - 1 item
 * - userAnswerNormalized = "Label1, Label2" (labels sélectionnés, séparés par
 * ", ")
 * - correctAnswer = "CorrectLabel1, CorrectLabel2" (labels corrects, même
 * format)
 *
 * BINARY_SWIPE:
 * - 1 item
 * - userAnswerNormalized = "Vrai" ou "Faux"
 * - correctAnswer = valeur attendue ("Vrai" ou "Faux")
 *
 * HANGMAN (multi-step):
 * - 1 item
 * - userAnswerNormalized = masque actuel (ex: "T _ f f _ n y")
 * - correctAnswer = solution complète (ex: "Tiffany")
 * - Note: état complet (wrongLetters, triedLetters, errorsCount) dans
 * currentState
 *
 * WORD_PUZZLE (multi-step):
 * - 1 item (dernière tentative uniquement)
 * - userAnswerNormalized = dernier mot tenté
 * - correctAnswer = solution
 * - Note: historique complet des tentatives dans currentState.attempts[]
 *
 * ASSOCIATION (futur):
 * - 1 item global
 * - userAnswerNormalized = représentation JSON des paires soumises
 * - correctAnswer = représentation JSON des paires correctes
 *
 * ORDERING (futur):
 * - 1 item global
 * - userAnswerNormalized = représentation de l'ordre soumis
 * - correctAnswer = représentation de l'ordre correct
 */
@Service
public class QuizAnswerItemResultBuilder {

    /**
     * Construit les itemResults pour l'affichage des corrections.
     *
     * @param snapshot Le snapshot figé de la question
     * @param eval     Le résultat d'évaluation contenant les données de correction
     * @return Liste de QuizAnswerItemResult (généralement 1 item, sauf cas
     *         spéciaux)
     */
    public List<QuizAnswerItemResult> build(
            QuizQuestionSnapshot snapshot,
            QuizEvaluationResult eval) {

        Objects.requireNonNull(snapshot, "snapshot");
        Objects.requireNonNull(eval, "eval");

        QuizFormat format = snapshot.getFormat();
        if (format == null) {
            return List.of();
        }

        return switch (format) {
            case TEXT_INPUT, CLOZE -> buildTextBased(snapshot, eval);
            case MCQ -> buildMcq(snapshot, eval);
            case BINARY_SWIPE -> buildBinarySwipe(snapshot, eval);
            case HANGMAN -> buildHangman(snapshot, eval);
            case WORD_PUZZLE -> buildWordPuzzle(snapshot, eval);
            case ASSOCIATION -> buildAssociation(snapshot, eval);
            case ORDERING -> buildOrdering(snapshot, eval);
        };
    }

    // ========================================
    // TEXT_INPUT / CLOZE
    // ========================================

    private List<QuizAnswerItemResult> buildTextBased(
            QuizQuestionSnapshot snapshot,
            QuizEvaluationResult eval) {

        String userAnswer = null;
        if (eval.normalizedAudit() instanceof NormalizedText nt) {
            userAnswer = nt.raw();
        }

        return List.of(new QuizAnswerItemResult.Builder()
                .withPosition(0)
                .withRole(QuizQuestionItemRole.TARGET)
                .withPersonId(snapshot.getPersonId())
                .withCorrect(eval.correct())
                .withUserAnswerNormalized(userAnswer)
                .withCorrectAnswer(eval.correctAnswerDisplay())
                .withTargetAnswerResult(eval.targetAnswerResult())
                .build());
    }

    // ========================================
    // MCQ
    // ========================================

    private List<QuizAnswerItemResult> buildMcq(
            QuizQuestionSnapshot snapshot,
            QuizEvaluationResult eval) {

        List<QuizChoice> choices = snapshot.getPayload() != null
                ? snapshot.getPayload().getChoices()
                : List.of();

        List<String> correctKeys = snapshot.getTruth() != null
                ? snapshot.getTruth().getCorrectChoiceKeys()
                : List.of();

        boolean allowMultiple = snapshot.getPayload() != null
                && Boolean.TRUE.equals(snapshot.getPayload().getAllowMultiple());

        // Trouver le(s) label(s) sélectionné(s)
        String userAnswerNormalized = null;
        if (eval.normalizedAudit() instanceof NormalizedChoice nc) {
            if (allowMultiple) {
                // Pour allowMultiple=true, on récupère via selectedValue pour l'instant
                // (selectedChoiceIds n'existe pas encore dans NormalizedChoice)
                userAnswerNormalized = nc.selectedValue();
            } else if (nc.selectedChoiceId() != null) {
                // Mode single: trouver le label du choix sélectionné
                userAnswerNormalized = choices.stream()
                        .filter(c -> Objects.equals(c.getId(), nc.selectedChoiceId()))
                        .map(QuizChoice::getLabel)
                        .findFirst()
                        .orElse(nc.selectedValue());
            }
        }

        // Trouver le(s) label(s) correct(s)
        String correctAnswer = choices.stream()
                .filter(c -> correctKeys.contains(String.valueOf(c.getId())))
                .map(QuizChoice::getLabel)
                .collect(Collectors.joining(", "));

        // Fallback sur correctAnswerDisplay si pas de choix trouvé
        if (correctAnswer.isEmpty() && eval.correctAnswerDisplay() != null) {
            correctAnswer = eval.correctAnswerDisplay();
        }

        return List.of(new QuizAnswerItemResult.Builder()
                .withPosition(0)
                .withRole(QuizQuestionItemRole.TARGET)
                .withPersonId(snapshot.getPersonId())
                .withCorrect(eval.correct())
                .withUserAnswerNormalized(userAnswerNormalized)
                .withCorrectAnswer(correctAnswer)
                .withTargetAnswerResult(eval.targetAnswerResult())
                .build());
    }

    // ========================================
    // BINARY_SWIPE
    // ========================================

    private List<QuizAnswerItemResult> buildBinarySwipe(
            QuizQuestionSnapshot snapshot,
            QuizEvaluationResult eval) {

        String userAnswer = null;
        if (eval.normalizedAudit() instanceof NormalizedBinary nb && nb.swipeRight() != null) {
            userAnswer = nb.swipeRight() ? "Vrai" : "Faux";
        }

        String correctAnswer = null;
        if (snapshot.getTruth() != null && snapshot.getTruth().getCorrectSwipeRight() != null) {
            correctAnswer = snapshot.getTruth().getCorrectSwipeRight() ? "Vrai" : "Faux";
        }

        return List.of(new QuizAnswerItemResult.Builder()
                .withPosition(0)
                .withRole(QuizQuestionItemRole.TARGET)
                .withPersonId(snapshot.getPersonId())
                .withCorrect(eval.correct())
                .withUserAnswerNormalized(userAnswer)
                .withCorrectAnswer(correctAnswer)
                .withTargetAnswerResult(eval.targetAnswerResult())
                .build());
    }

    // ========================================
    // HANGMAN (multi-step)
    // ========================================

    private List<QuizAnswerItemResult> buildHangman(
            QuizQuestionSnapshot snapshot,
            QuizEvaluationResult eval) {

        String mask = null;
        if (eval.updatedState() instanceof HangmanSnapshotState state) {
            mask = state.getMask();
        } else if (snapshot.getHangmanState() != null) {
            mask = snapshot.getHangmanState().getMask();
        }

        return List.of(new QuizAnswerItemResult.Builder()
                .withPosition(0)
                .withRole(QuizQuestionItemRole.TARGET)
                .withPersonId(snapshot.getPersonId())
                .withCorrect(eval.correct())
                .withUserAnswerNormalized(mask)
                .withCorrectAnswer(eval.correctAnswerDisplay())
                .withTargetAnswerResult(eval.targetAnswerResult())
                .build());
    }

    // ========================================
    // WORD_PUZZLE (multi-step)
    // ========================================

    private List<QuizAnswerItemResult> buildWordPuzzle(
            QuizQuestionSnapshot snapshot,
            QuizEvaluationResult eval) {

        String lastAttempt = null;

        // Récupérer la dernière tentative depuis l'état mis à jour ou le snapshot
        WordPuzzleSnapshotState state = null;
        if (eval.updatedState() instanceof WordPuzzleSnapshotState updatedState) {
            state = updatedState;
        } else if (snapshot.getWordPuzzleState() != null) {
            state = snapshot.getWordPuzzleState();
        }

        if (state != null && state.getAttempts() != null && !state.getAttempts().isEmpty()) {
            // Dernière tentative
            var attempts = state.getAttempts();
            lastAttempt = attempts.get(attempts.size() - 1).getWord();
        }

        return List.of(new QuizAnswerItemResult.Builder()
                .withPosition(0)
                .withRole(QuizQuestionItemRole.TARGET)
                .withPersonId(snapshot.getPersonId())
                .withCorrect(eval.correct())
                .withUserAnswerNormalized(lastAttempt)
                .withCorrectAnswer(eval.correctAnswerDisplay())
                .withTargetAnswerResult(eval.targetAnswerResult())
                .build());
    }

    // ========================================
    // ASSOCIATION (placeholder - futur)
    // ========================================

    private List<QuizAnswerItemResult> buildAssociation(
            QuizQuestionSnapshot snapshot,
            QuizEvaluationResult eval) {

        // TODO: Implémenter quand le frontend en aura besoin
        // Pour l'instant, retourner un item simple avec correctAnswerDisplay
        return List.of(new QuizAnswerItemResult.Builder()
                .withPosition(0)
                .withRole(QuizQuestionItemRole.TARGET)
                .withCorrect(eval.correct())
                .withCorrectAnswer(eval.correctAnswerDisplay())
                .withTargetAnswerResult(eval.targetAnswerResult())
                .build());
    }

    // ========================================
    // ORDERING (placeholder - futur)
    // ========================================

    private List<QuizAnswerItemResult> buildOrdering(
            QuizQuestionSnapshot snapshot,
            QuizEvaluationResult eval) {

        // TODO: Implémenter quand le frontend en aura besoin
        // Pour l'instant, retourner un item simple avec correctAnswerDisplay
        return List.of(new QuizAnswerItemResult.Builder()
                .withPosition(0)
                .withRole(QuizQuestionItemRole.TARGET)
                .withCorrect(eval.correct())
                .withCorrectAnswer(eval.correctAnswerDisplay())
                .withTargetAnswerResult(eval.targetAnswerResult())
                .build());
    }
}
