// src/main/java/com/saymyname/core/model/quiz/QuizQuestion.java
package com.saymyname.core.model.quiz;

import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizFormat;

public class QuizQuestion {

    private String questionToken; // ✅ NEW (training truth token)

    private Long personId;
    private String storageKey;

    private Long gameModeId;
    private List<Long> targetAttributeIds;
    private String operator;

    private QuizQuestionContext context;

    private QuizFormat format;

    private QuizQuestionPayload payload;
    private QuizQuestionHints hints;
    private QuizQuestionDisplay display;
    private QuizFollowUp followUp;
    private QuizDecisionReasonCode reasonCode;
    private String reasonDetailsJson;

    public QuizQuestion() {
    }

    private QuizQuestion(Builder b) {
        this.questionToken = b.questionToken;
        this.personId = b.personId;
        this.storageKey = b.storageKey;
        this.gameModeId = b.gameModeId;
        this.targetAttributeIds = b.targetAttributeIds;
        this.operator = b.operator;
        this.context = b.context;
        this.format = b.format;
        this.payload = b.payload;
        this.hints = b.hints;
        this.display = b.display;
        this.followUp = b.followUp;
        this.reasonCode = b.reasonCode;
        this.reasonDetailsJson = b.reasonDetailsJson;
    }

    public String getQuestionToken() {
        return questionToken;
    }

    public void setQuestionToken(String questionToken) {
        this.questionToken = questionToken;
    }

    public Long getPersonId() {
        return personId;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public Long getGameModeId() {
        return gameModeId;
    }

    public List<Long> getTargetAttributeIds() {
        return targetAttributeIds;
    }

    public String getOperator() {
        return operator;
    }

    public QuizQuestionContext getContext() {
        return context;
    }

    public QuizFormat getFormat() {
        return format;
    }

    public QuizQuestionPayload getPayload() {
        return payload;
    }

    public QuizQuestionHints getHints() {
        return hints;
    }

    public QuizQuestionDisplay getDisplay() {
        return display;
    }

    public QuizFollowUp getFollowUp() {
        return followUp;
    }

    public QuizDecisionReasonCode getReasonCode() {
        return reasonCode;
    }

    public String getReasonDetailsJson() {
        return reasonDetailsJson;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public void setGameModeId(Long gameModeId) {
        this.gameModeId = gameModeId;
    }

    public void setTargetAttributeIds(List<Long> targetAttributeIds) {
        this.targetAttributeIds = targetAttributeIds;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setContext(QuizQuestionContext context) {
        this.context = context;
    }

    public void setFormat(QuizFormat format) {
        this.format = format;
    }

    public void setPayload(QuizQuestionPayload payload) {
        this.payload = payload;
    }

    public void setHints(QuizQuestionHints hints) {
        this.hints = hints;
    }

    public void setDisplay(QuizQuestionDisplay display) {
        this.display = display;
    }

    public void setFollowUp(QuizFollowUp followUp) {
        this.followUp = followUp;
    }

    public void setReasonCode(QuizDecisionReasonCode reasonCode) {
        this.reasonCode = reasonCode;
    }

    public void setReasonDetailsJson(String reasonDetailsJson) {
        this.reasonDetailsJson = reasonDetailsJson;
    }

    public static class Builder {
        private String questionToken;

        private Long personId;
        private String storageKey;
        private Long gameModeId;
        private List<Long> targetAttributeIds;
        private String operator;
        private QuizQuestionContext context;
        private QuizFormat format;

        private QuizQuestionPayload payload;
        private QuizQuestionHints hints;
        private QuizQuestionDisplay display;
        private QuizFollowUp followUp;
        private QuizDecisionReasonCode reasonCode;
        private String reasonDetailsJson;

        public Builder withQuestionToken(String v) {
            this.questionToken = v;
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

        public Builder withContext(QuizQuestionContext v) {
            this.context = v;
            return this;
        }

        public Builder withFormat(QuizFormat v) {
            this.format = v;
            return this;
        }

        public Builder withPayload(QuizQuestionPayload v) {
            this.payload = v;
            return this;
        }

        public Builder withHints(QuizQuestionHints v) {
            this.hints = v;
            return this;
        }

        public Builder withDisplay(QuizQuestionDisplay v) {
            this.display = v;
            return this;
        }

        public Builder withFollowUp(QuizFollowUp v) {
            this.followUp = v;
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

        public QuizQuestion build() {
            return new QuizQuestion(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizQuestion))
            return false;
        QuizQuestion that = (QuizQuestion) o;
        return Objects.equals(questionToken, that.questionToken)
                && Objects.equals(personId, that.personId)
                && Objects.equals(storageKey, that.storageKey)
                && Objects.equals(gameModeId, that.gameModeId)
                && Objects.equals(targetAttributeIds, that.targetAttributeIds)
                && Objects.equals(operator, that.operator)
                && Objects.equals(context, that.context)
                && format == that.format
                && Objects.equals(payload, that.payload)
                && Objects.equals(hints, that.hints)
                && Objects.equals(display, that.display)
                && Objects.equals(followUp, that.followUp)
                && Objects.equals(reasonCode, that.reasonCode)
                && Objects.equals(reasonDetailsJson, that.reasonDetailsJson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                questionToken,
                personId,
                storageKey,
                gameModeId,
                targetAttributeIds,
                operator,
                context,
                format,
                payload,
                hints,
                display,
                followUp,
                reasonCode,
                reasonDetailsJson);
    }

    @Override
    public String toString() {
        return "QuizQuestion{" +
                "questionToken='" + questionToken + '\'' +
                ", personId=" + personId +
                ", storageKey='" + storageKey + '\'' +
                ", gameModeId=" + gameModeId +
                ", targetAttributeIds=" + targetAttributeIds +
                ", operator='" + operator + '\'' +
                ", context=" + context +
                ", format=" + format +
                ", payload=" + payload +
                ", hints=" + hints +
                ", display=" + display +
                ", followUp=" + followUp +
                ", reasonCode='" + reasonCode + '\'' +
                ", reasonDetailsJson='" + reasonDetailsJson + '\'' +
                '}';
    }
}
