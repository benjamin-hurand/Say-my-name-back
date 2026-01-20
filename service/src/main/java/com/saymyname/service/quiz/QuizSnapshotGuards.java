package com.saymyname.service.quiz;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.QuizQuestionContext;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;

public final class QuizSnapshotGuards {

    /**
     * Formats dont la validation dépend d'une vérité EAV (targetAttributeIds).
     * => Inclut aussi les formats TEXT (TEXT_INPUT/CLOZE/HANGMAN) car truth est
     * figée à partir des attributs.
     */
    private static final EnumSet<QuizFormat> FORMATS_REQUIRING_TARGET_ATTRIBUTE_IDS = EnumSet.of(
            QuizFormat.TEXT_INPUT,
            QuizFormat.CLOZE,
            QuizFormat.HANGMAN,
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

        if (requiresTargetAttributeIds(fmt)) {
            requireSingleTargetAttributeId(snapshot, fmt);
        }
    }

    public static boolean requiresTargetAttributeIds(QuizFormat format) {
        return format != null && FORMATS_REQUIRING_TARGET_ATTRIBUTE_IDS.contains(format);
    }

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
