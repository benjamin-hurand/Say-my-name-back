package com.saymyname.service.quiz;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.QuizQuestionContext;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;

public final class QuizSnapshotGuards {

    /**
     * Formats dont la validation (et/ou l’UI) dépend explicitement d’un attributeId
     * (donc targetAttributeIds non vide, non null, sans null).
     */
    private static final EnumSet<QuizFormat> FORMATS_REQUIRING_TARGET_ATTRIBUTE_IDS = EnumSet.of(
            QuizFormat.MCQ,
            QuizFormat.BINARY_SWIPE,
            QuizFormat.ASSOCIATION,
            QuizFormat.ORDERING);

    private QuizSnapshotGuards() {
    }

    public static void validateSnapshotOrThrow(QuizQuestionSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalStateException("Invalid QuizQuestionSnapshot for answer: snapshot=null");
        }

        // Valide d'abord invariants "structuraux" du snapshot (schéma, champs requis,
        // etc.)
        try {
            snapshot.validateInvariants();
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "Invalid QuizQuestionSnapshot for answer: " + contextSummary(snapshot), e);
        }

        // Valide ensuite les invariants dépendants du format.
        validateFormatSpecificInvariants(snapshot);
    }

    /**
     * Validation stricte par format :
     * - format non null
     * - si le format exige targetAttributeIds => fail-fast si manquant/invalid
     */
    private static void validateFormatSpecificInvariants(QuizQuestionSnapshot snapshot) {
        QuizFormat fmt = snapshot.getFormat();
        if (fmt == null) {
            throw new IllegalStateException("Missing snapshot.format " + contextSummary(snapshot));
        }

        if (requiresTargetAttributeIds(fmt)) {
            // Fail-fast (et message contextualisé) si targetAttributeIds est invalide.
            requireSingleTargetAttributeId(snapshot, fmt);
        }
    }

    public static boolean requiresTargetAttributeIds(QuizFormat format) {
        return format != null && FORMATS_REQUIRING_TARGET_ATTRIBUTE_IDS.contains(format);
    }

    /**
     * Exige un seul attributeId "principal" exploitable côté validation/DTO.
     * On tolère que la liste contienne plusieurs ids, mais :
     * - elle doit exister
     * - non vide
     * - aucun élément null
     * - le premier élément (id principal) non null
     */
    public static Long requireSingleTargetAttributeId(QuizQuestionSnapshot snapshot, QuizFormat format) {
        Objects.requireNonNull(snapshot, "snapshot");

        List<Long> ids = snapshot.getTargetAttributeIds();
        if (ids == null || ids.isEmpty() || ids.get(0) == null || ids.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException(
                    "Missing targetAttributeIds for format=" + format + " " + contextSummary(snapshot));
        }
        return ids.get(0);
    }

    public static String contextSummary(QuizQuestionSnapshot snapshot) {
        if (snapshot == null) {
            return "snapshot=null";
        }

        QuizQuestionContext ctx = snapshot.getContext();

        return "format=" + snapshot.getFormat()
                + ", source=" + (ctx != null ? ctx.getSource() : null)
                + ", courseId=" + (ctx != null ? ctx.getCourseId() : null)
                + ", courseQuestionId=" + (ctx != null ? ctx.getCourseQuestionId() : null)
                + ", questionRound=" + (ctx != null ? ctx.getQuestionRound() : null)
                + ", poolType=" + (ctx != null ? ctx.getPoolType() : null)
                + ", reducedOptionsId=" + (ctx != null ? ctx.getReducedOptionsId() : null)
                + ", targetPersonIds=" + snapshot.getTargetPersonIds()
                + ", targetAttributeIds=" + snapshot.getTargetAttributeIds();
    }
}
