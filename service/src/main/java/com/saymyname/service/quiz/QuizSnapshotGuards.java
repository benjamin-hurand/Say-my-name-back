package com.saymyname.service.quiz;

import java.util.EnumSet;
import java.util.Objects;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.QuizQuestionContext;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;

public final class QuizSnapshotGuards {

    /**
     * Formats dont la validation dépend d'une vérité EAV (targetAttributeId).
     * => Inclut aussi les formats TEXT (TEXT_INPUT/CLOZE/HANGMAN/WORD_PUZZLE) car
     * truth est
     * figée à partir des attributs.
     */
    private static final EnumSet<QuizFormat> FORMATS_REQUIRING_TARGET_ATTRIBUTE_ID = EnumSet.of(
            QuizFormat.TEXT_INPUT,
            QuizFormat.CLOZE,
            QuizFormat.HANGMAN,
            QuizFormat.WORD_PUZZLE,
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

        try {
            snapshot.validateInvariants();
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "Invalid QuizQuestionSnapshot for answer: " + contextSummary(snapshot), e);
        }

        validateFormatSpecificInvariants(snapshot);
    }

    private static void validateFormatSpecificInvariants(QuizQuestionSnapshot snapshot) {
        QuizFormat fmt = snapshot.getFormat();
        if (fmt == null) {
            throw new IllegalStateException("Missing snapshot.format " + contextSummary(snapshot));
        }

        if (requiresTargetAttributeId(fmt)) {
            requireTargetAttributeId(snapshot, fmt);
        }
    }

    public static boolean requiresTargetAttributeId(QuizFormat format) {
        return format != null && FORMATS_REQUIRING_TARGET_ATTRIBUTE_ID.contains(format);
    }

    public static Long requireTargetAttributeId(QuizQuestionSnapshot snapshot, QuizFormat format) {
        Objects.requireNonNull(snapshot, "snapshot");

        Long id = snapshot.getTargetAttributeId();
        if (id == null || id <= 0) {
            throw new IllegalStateException(
                    "Missing targetAttributeId for format=" + format + " " + contextSummary(snapshot));
        }
        return id;
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
                + ", targetAttributeId=" + snapshot.getTargetAttributeId();
    }
}