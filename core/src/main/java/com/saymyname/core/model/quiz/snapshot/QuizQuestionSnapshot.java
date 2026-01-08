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
    private Long gameModeId;
    private List<Long> targetAttributeIds = new ArrayList<>();
    private String operator;

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

    public QuizQuestionSnapshot() {
    }

    private QuizQuestionSnapshot(Builder b) {

        this.snapshotSchemaVersion = b.snapshotSchemaVersion;
        this.generatorVersion = b.generatorVersion;
        this.normalizerVersion = b.normalizerVersion;

        this.format = b.format;
        this.context = b.context;

        this.gameModeId = b.gameModeId;
        this.targetAttributeIds = b.targetAttributeIds != null ? b.targetAttributeIds : new ArrayList<>();
        this.operator = b.operator;
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

        // ---- auditable spec ----
        if (gameModeId == null)
            throw new IllegalStateException("QuizQuestionSnapshot.gameModeId is required");

        if (targetAttributeIds == null || targetAttributeIds.isEmpty())
            throw new IllegalStateException(
                    "QuizQuestionSnapshot.targetAttributeIds must contain at least 1 attributeId");

        if (targetAttributeIds.stream().anyMatch(Objects::isNull))
            throw new IllegalStateException("QuizQuestionSnapshot.targetAttributeIds cannot contain null");

        if (operator == null || operator.isBlank())
            throw new IllegalStateException("QuizQuestionSnapshot.operator is required");

        // personId/storageKey peuvent être null si multi-target natif / non applicable

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
        // Si personId est fourni, il doit être inclus dans targetPersonIds (au moins).
        if (personId != null && !targetPersonIds.contains(personId)) {
            throw new IllegalStateException(
                    "QuizQuestionSnapshot.personId must be included in targetPersonIds when provided");
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

    public Long getGameModeId() {
        return gameModeId;
    }

    public void setGameModeId(Long gameModeId) {
        this.gameModeId = gameModeId;
    }

    public List<Long> getTargetAttributeIds() {
        return targetAttributeIds;
    }

    public void setTargetAttributeIds(List<Long> targetAttributeIds) {
        this.targetAttributeIds = targetAttributeIds;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
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

    public Boolean getTimed() {
        return timed;
    }

    public Integer getTimeLimitMs() {
        return timeLimitMs;
    }

    public QuizDecisionReasonCode getReasonCode() {
        return reasonCode;
    }

    public String getReasonDetailsJson() {
        return reasonDetailsJson;
    }

    public void setFollowUp(QuizFollowUp followUp) {
        this.followUp = followUp;
    }

    public void setTimed(Boolean timed) {
        this.timed = timed;
    }

    public void setTimeLimitMs(Integer timeLimitMs) {
        this.timeLimitMs = timeLimitMs;
    }

    public void setReasonCode(QuizDecisionReasonCode reasonCode) {
        this.reasonCode = reasonCode;
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

    // ---------------- Builder ----------------

    public static class Builder {
        private int snapshotSchemaVersion;
        private String generatorVersion;
        private String normalizerVersion;

        private QuizFormat format;
        private QuizQuestionContext context;

        private Long gameModeId;
        private List<Long> targetAttributeIds;
        private String operator;
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

        public Builder withGameModeId(Long v) {
            this.gameModeId = v;
            return this;
        }

        public Builder withTargetAttributeIds(List<Long> v) {
            this.targetAttributeIds = v;
            return this;
        }

        public Builder withOperator(String v) {
            this.operator = v;
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
                && Objects.equals(gameModeId, that.gameModeId)
                && Objects.equals(targetAttributeIds, that.targetAttributeIds)
                && Objects.equals(operator, that.operator)
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
                && Objects.equals(targetPersonIds, that.targetPersonIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                snapshotSchemaVersion,
                generatorVersion,
                normalizerVersion,
                format,
                context,
                gameModeId,
                targetAttributeIds,
                operator,
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
                targetPersonIds);
    }
}
