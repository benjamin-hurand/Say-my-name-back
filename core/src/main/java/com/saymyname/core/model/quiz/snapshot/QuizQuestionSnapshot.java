// src/main/java/com/saymyname/core/model/quiz/snapshot/QuizQuestionSnapshot.java
package com.saymyname.core.model.quiz.snapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.QuizFollowUp;
import com.saymyname.core.model.quiz.QuizQuestionContext;
import com.saymyname.core.model.quiz.QuizQuestionDisplay;
import com.saymyname.core.model.quiz.QuizQuestionHints;
import com.saymyname.core.model.quiz.QuizQuestionPayload;

/**
 * Immutable snapshot (Option B) : doit être self-contained pour
 * audit/replay/debug.
 * Contient la représentation affichée (display/hints/payload), la vérité
 * (truth),
 * le contexte (course/training), et une spec minimale auditable.
 */
public class QuizQuestionSnapshot {

    // ---- identity / versions ----
    private int snapshotSchemaVersion;
    private String generatorVersion;
    private String normalizerVersion;

    // ---- format + context ----
    private QuizFormat format;
    private QuizQuestionContext context;

    // ---- auditable spec (reconstruction sans dépendre de la DB/builder) ----
    private Long targetAttributeId;

    /**
     * Person “principal” (utile pour compat legacy / analytics).
     * Peut être null si la question est nativement multi-target.
     */
    private Long personId;

    /**
     * Pour replay UI exact (image principale). Peut être null si non applicable.
     * Exemple: storageKey photo APPROVED.
     */
    private String storageKey;

    // ---- renderable parts ----
    private QuizQuestionDisplay display;
    private QuizQuestionHints hints;
    private QuizQuestionPayload payload;
    private QuizFollowUp followUp;

    // ---- timing ----
    private Boolean timed;
    private Integer timeLimitMs;

    // ---- reason ----
    private QuizDecisionReasonCode reasonCode;
    private String reasonDetailsJson;

    // ---- truth ----
    private QuizQuestionTruth truth;

    // ---- multi-target explicit ----
    private List<Long> targetPersonIds = new ArrayList<>();

    // ------------------------------------------------------------------
    // HANGMAN (stateful, snapshot-based)
    // ------------------------------------------------------------------
    /** Règles Hangman (paramétrage stable) - requis si format=HANGMAN. */
    private HangmanRules hangmanRules;

    /** État Hangman (mask + erreurs + lettres) - requis si format=HANGMAN. */
    private HangmanSnapshotState hangmanState;

    // ------------------------------------------------------------------
    // WORD_PUZZLE (stateful, snapshot-based)
    // ------------------------------------------------------------------
    /** Règles Word Puzzle (paramétrage stable) - requis si format=WORD_PUZZLE. */
    private WordPuzzleRules wordPuzzleRules;

    /**
     * État Word Puzzle (attempts + feedback + status) - requis si
     * format=WORD_PUZZLE.
     */
    private WordPuzzleSnapshotState wordPuzzleState;

    public QuizQuestionSnapshot() {
    }

    private QuizQuestionSnapshot(Builder b) {

        this.snapshotSchemaVersion = b.snapshotSchemaVersion;
        this.generatorVersion = b.generatorVersion;
        this.normalizerVersion = b.normalizerVersion;

        this.format = b.format;
        this.context = b.context;

        this.targetAttributeId = b.targetAttributeId;

        this.personId = b.personId;
        this.storageKey = b.storageKey;

        this.display = b.display;
        this.hints = b.hints;
        this.payload = b.payload;
        this.followUp = b.followUp;
        this.timed = b.timed;
        this.timeLimitMs = b.timeLimitMs;
        this.reasonCode = b.reasonCode;
        this.reasonDetailsJson = b.reasonDetailsJson;

        this.truth = b.truth;
        this.targetPersonIds = b.targetPersonIds != null ? b.targetPersonIds : new ArrayList<>();

        this.hangmanRules = b.hangmanRules;
        this.hangmanState = b.hangmanState;

        this.wordPuzzleRules = b.wordPuzzleRules;
        this.wordPuzzleState = b.wordPuzzleState;

        validateInvariants();
    }

    public void validateInvariants() {
        if (snapshotSchemaVersion <= 0)
            throw new IllegalStateException("QuizQuestionSnapshot.snapshotSchemaVersion must be >= 1");

        if (generatorVersion == null || generatorVersion.isBlank())
            throw new IllegalStateException("QuizQuestionSnapshot.generatorVersion is required");

        if (normalizerVersion == null || normalizerVersion.isBlank())
            throw new IllegalStateException("QuizQuestionSnapshot.normalizerVersion is required");

        if (format == null)
            throw new IllegalStateException("QuizQuestionSnapshot.format is required");

        if (context == null)
            throw new IllegalStateException("QuizQuestionSnapshot.context is required");

        if (targetAttributeId == null)
            throw new IllegalStateException(
                    "QuizQuestionSnapshot.targetAttributeId must contain at least 1 attributeId");

        // ---- render + truth ----
        if (payload == null)
            throw new IllegalStateException("QuizQuestionSnapshot.payload is required");

        if (Boolean.TRUE.equals(timed)) {
            if (timeLimitMs == null || timeLimitMs < 1000) {
                throw new IllegalStateException("QuizQuestionSnapshot.timeLimitMs must be >= 1000 when timed=true");
            }
        }

        if (truth == null)
            throw new IllegalStateException("QuizQuestionSnapshot.truth is required");

        truth.validateInvariants();

        // ---- targets ----
        if (targetPersonIds == null || targetPersonIds.isEmpty())
            throw new IllegalStateException("QuizQuestionSnapshot.targetPersonIds must contain at least 1 personId");

        if (targetPersonIds.stream().anyMatch(Objects::isNull))
            throw new IllegalStateException("QuizQuestionSnapshot.targetPersonIds cannot contain null");

        // ---- coherence guard ----
        if (personId != null && !targetPersonIds.contains(personId)) {
            throw new IllegalStateException(
                    "QuizQuestionSnapshot.personId must be included in targetPersonIds when provided");
        }

        // ------------------------------------------------------------------
        // HANGMAN invariants
        // ------------------------------------------------------------------
        if (format == QuizFormat.HANGMAN) {
            if (hangmanRules == null) {
                throw new IllegalStateException("QuizQuestionSnapshot.hangmanRules is required when format=HANGMAN");
            }
            if (hangmanState == null) {
                throw new IllegalStateException("QuizQuestionSnapshot.hangmanState is required when format=HANGMAN");
            }
            hangmanRules.validateInvariants();
            hangmanState.validateInvariants();

            Integer max = hangmanRules.getMaxErrors();
            Integer errors = hangmanState.getErrorsCount();
            if (max != null && errors != null && errors > max) {
                throw new IllegalStateException(
                        "QuizQuestionSnapshot.hangmanState.errorsCount cannot exceed hangmanRules.maxErrors");
            }
        }

        // ------------------------------------------------------------------
        // WORD_PUZZLE invariants
        // ------------------------------------------------------------------
        if (format == QuizFormat.WORD_PUZZLE) {
            if (wordPuzzleRules == null) {
                throw new IllegalStateException(
                        "QuizQuestionSnapshot.wordPuzzleRules is required when format=WORD_PUZZLE");
            }
            if (wordPuzzleState == null) {
                throw new IllegalStateException(
                        "QuizQuestionSnapshot.wordPuzzleState is required when format=WORD_PUZZLE");
            }
            wordPuzzleRules.validateInvariants();
            wordPuzzleState.validateInvariants();

            Integer maxAttempts = wordPuzzleRules.getMaxAttempts();
            Integer attemptsRemaining = wordPuzzleState.getAttemptsRemaining();
            int attemptsMade = wordPuzzleState.getAttempts().size();
            if (maxAttempts != null && attemptsRemaining != null) {
                int expected = maxAttempts - attemptsMade;
                if (attemptsRemaining != expected) {
                    throw new IllegalStateException(
                            "QuizQuestionSnapshot.wordPuzzleState.attemptsRemaining inconsistent: expected " + expected
                                    + ", got " + attemptsRemaining);
                }
            }
        }
    }

    // ---------------- Getters/Setters ----------------
    public int getSnapshotSchemaVersion() {
        return snapshotSchemaVersion;
    }

    public void setSnapshotSchemaVersion(int snapshotSchemaVersion) {
        this.snapshotSchemaVersion = snapshotSchemaVersion;
    }

    public String getGeneratorVersion() {
        return generatorVersion;
    }

    public void setGeneratorVersion(String generatorVersion) {
        this.generatorVersion = generatorVersion;
    }

    public String getNormalizerVersion() {
        return normalizerVersion;
    }

    public void setNormalizerVersion(String normalizerVersion) {
        this.normalizerVersion = normalizerVersion;
    }

    public QuizFormat getFormat() {
        return format;
    }

    public void setFormat(QuizFormat format) {
        this.format = format;
    }

    public QuizQuestionContext getContext() {
        return context;
    }

    public void setContext(QuizQuestionContext context) {
        this.context = context;
    }

    public Long getTargetAttributeId() {
        return targetAttributeId;
    }

    public void setTargetAttributeId(Long targetAttributeId) {
        this.targetAttributeId = targetAttributeId;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public QuizQuestionDisplay getDisplay() {
        return display;
    }

    public void setDisplay(QuizQuestionDisplay display) {
        this.display = display;
    }

    public QuizQuestionHints getHints() {
        return hints;
    }

    public void setHints(QuizQuestionHints hints) {
        this.hints = hints;
    }

    public QuizQuestionPayload getPayload() {
        return payload;
    }

    public void setPayload(QuizQuestionPayload payload) {
        this.payload = payload;
    }

    public QuizFollowUp getFollowUp() {
        return followUp;
    }

    public void setFollowUp(QuizFollowUp followUp) {
        this.followUp = followUp;
    }

    public Boolean getTimed() {
        return timed;
    }

    public void setTimed(Boolean timed) {
        this.timed = timed;
    }

    public Integer getTimeLimitMs() {
        return timeLimitMs;
    }

    public void setTimeLimitMs(Integer timeLimitMs) {
        this.timeLimitMs = timeLimitMs;
    }

    public QuizDecisionReasonCode getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(QuizDecisionReasonCode reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getReasonDetailsJson() {
        return reasonDetailsJson;
    }

    public void setReasonDetailsJson(String reasonDetailsJson) {
        this.reasonDetailsJson = reasonDetailsJson;
    }

    public QuizQuestionTruth getTruth() {
        return truth;
    }

    public void setTruth(QuizQuestionTruth truth) {
        this.truth = truth;
    }

    public List<Long> getTargetPersonIds() {
        return targetPersonIds;
    }

    public void setTargetPersonIds(List<Long> targetPersonIds) {
        this.targetPersonIds = targetPersonIds;
    }

    // ---- Hangman getters/setters ----
    public HangmanRules getHangmanRules() {
        return hangmanRules;
    }

    public void setHangmanRules(HangmanRules hangmanRules) {
        this.hangmanRules = hangmanRules;
    }

    public HangmanSnapshotState getHangmanState() {
        return hangmanState;
    }

    public void setHangmanState(HangmanSnapshotState hangmanState) {
        this.hangmanState = hangmanState;
    }

    // ---- Word Puzzle getters/setters ----
    public WordPuzzleRules getWordPuzzleRules() {
        return wordPuzzleRules;
    }

    public void setWordPuzzleRules(WordPuzzleRules wordPuzzleRules) {
        this.wordPuzzleRules = wordPuzzleRules;
    }

    public WordPuzzleSnapshotState getWordPuzzleState() {
        return wordPuzzleState;
    }

    public void setWordPuzzleState(WordPuzzleSnapshotState wordPuzzleState) {
        this.wordPuzzleState = wordPuzzleState;
    }

    // ---------------- Builder ----------------
    public static class Builder {
        private int snapshotSchemaVersion;
        private String generatorVersion;
        private String normalizerVersion;

        private QuizFormat format;
        private QuizQuestionContext context;

        private Long targetAttributeId;

        private Long personId;
        private String storageKey;

        private QuizQuestionDisplay display;
        private QuizQuestionHints hints;
        private QuizQuestionPayload payload;
        private QuizFollowUp followUp;
        private Boolean timed;
        private Integer timeLimitMs;
        private QuizDecisionReasonCode reasonCode;
        private String reasonDetailsJson;

        private QuizQuestionTruth truth;
        private List<Long> targetPersonIds;

        private HangmanRules hangmanRules;
        private HangmanSnapshotState hangmanState;

        private WordPuzzleRules wordPuzzleRules;
        private WordPuzzleSnapshotState wordPuzzleState;

        public Builder from(QuizQuestionSnapshot original) {
            this.snapshotSchemaVersion = original.snapshotSchemaVersion;
            this.generatorVersion = original.generatorVersion;
            this.normalizerVersion = original.normalizerVersion;
            this.format = original.format;
            this.context = original.context;

            this.targetAttributeId = original.targetAttributeId;

            this.personId = original.personId;
            this.storageKey = original.storageKey;

            this.display = original.display;
            this.hints = original.hints;
            this.payload = original.payload;
            this.followUp = original.followUp;
            this.timed = original.timed;
            this.timeLimitMs = original.timeLimitMs;
            this.reasonCode = original.reasonCode;
            this.reasonDetailsJson = original.reasonDetailsJson;

            this.truth = original.truth;
            this.targetPersonIds = original.targetPersonIds;

            this.hangmanRules = original.hangmanRules;
            this.hangmanState = original.hangmanState;

            this.wordPuzzleRules = original.wordPuzzleRules;
            this.wordPuzzleState = original.wordPuzzleState;

            return this;
        }

        public Builder withSnapshotSchemaVersion(int v) {
            this.snapshotSchemaVersion = v;
            return this;
        }

        public Builder withGeneratorVersion(String v) {
            this.generatorVersion = v;
            return this;
        }

        public Builder withNormalizerVersion(String v) {
            this.normalizerVersion = v;
            return this;
        }

        public Builder withFormat(QuizFormat v) {
            this.format = v;
            return this;
        }

        public Builder withContext(QuizQuestionContext v) {
            this.context = v;
            return this;
        }

        public Builder withTargetAttributeId(Long v) {
            this.targetAttributeId = v;
            return this;
        }

        public Builder withPersonId(Long v) {
            this.personId = v;
            return this;
        }

        public Builder withStorageKey(String v) {
            this.storageKey = v;
            return this;
        }

        public Builder withDisplay(QuizQuestionDisplay v) {
            this.display = v;
            return this;
        }

        public Builder withHints(QuizQuestionHints v) {
            this.hints = v;
            return this;
        }

        public Builder withPayload(QuizQuestionPayload v) {
            this.payload = v;
            return this;
        }

        public Builder withFollowUp(QuizFollowUp v) {
            this.followUp = v;
            return this;
        }

        public Builder withTimed(Boolean v) {
            this.timed = v;
            return this;
        }

        public Builder withTimeLimitMs(Integer v) {
            this.timeLimitMs = v;
            return this;
        }

        public Builder withReasonCode(QuizDecisionReasonCode v) {
            this.reasonCode = v;
            return this;
        }

        public Builder withReasonDetailsJson(String v) {
            this.reasonDetailsJson = v;
            return this;
        }

        public Builder withTruth(QuizQuestionTruth v) {
            this.truth = v;
            return this;
        }

        public Builder withTargetPersonIds(List<Long> v) {
            this.targetPersonIds = v;
            return this;
        }

        public Builder withHangmanRules(HangmanRules v) {
            this.hangmanRules = v;
            return this;
        }

        public Builder withHangmanState(HangmanSnapshotState v) {
            this.hangmanState = v;
            return this;
        }

        public Builder withWordPuzzleRules(WordPuzzleRules v) {
            this.wordPuzzleRules = v;
            return this;
        }

        public Builder withWordPuzzleState(WordPuzzleSnapshotState v) {
            this.wordPuzzleState = v;
            return this;
        }

        public QuizQuestionSnapshot build() {
            return new QuizQuestionSnapshot(this);
        }
    }

    // ---------------- equals/hashCode ----------------
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizQuestionSnapshot))
            return false;
        QuizQuestionSnapshot that = (QuizQuestionSnapshot) o;
        return snapshotSchemaVersion == that.snapshotSchemaVersion
                && Objects.equals(generatorVersion, that.generatorVersion)
                && Objects.equals(normalizerVersion, that.normalizerVersion)
                && format == that.format
                && Objects.equals(context, that.context)
                && Objects.equals(targetAttributeId, that.targetAttributeId)
                && Objects.equals(personId, that.personId)
                && Objects.equals(storageKey, that.storageKey)
                && Objects.equals(display, that.display)
                && Objects.equals(hints, that.hints)
                && Objects.equals(payload, that.payload)
                && Objects.equals(followUp, that.followUp)
                && Objects.equals(timed, that.timed)
                && Objects.equals(timeLimitMs, that.timeLimitMs)
                && Objects.equals(reasonCode, that.reasonCode)
                && Objects.equals(reasonDetailsJson, that.reasonDetailsJson)
                && Objects.equals(truth, that.truth)
                && Objects.equals(targetPersonIds, that.targetPersonIds)
                && Objects.equals(hangmanRules, that.hangmanRules)
                && Objects.equals(hangmanState, that.hangmanState)
                && Objects.equals(wordPuzzleRules, that.wordPuzzleRules)
                && Objects.equals(wordPuzzleState, that.wordPuzzleState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                snapshotSchemaVersion,
                generatorVersion,
                normalizerVersion,
                format,
                context,
                targetAttributeId,
                personId,
                storageKey,
                display,
                hints,
                payload,
                followUp,
                timed,
                timeLimitMs,
                reasonCode,
                reasonDetailsJson,
                truth,
                targetPersonIds,
                hangmanRules,
                hangmanState,
                wordPuzzleRules,
                wordPuzzleState);
    }
}