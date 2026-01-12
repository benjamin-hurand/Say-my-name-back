// src/main/java/com/saymyname/core/model/quiz/QuizQuestionPayload.java
package com.saymyname.core.model.quiz;

import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.enums.quiz.QuizOrderingRule;
import com.saymyname.core.model.enums.quiz.QuizPayloadType;

public class QuizQuestionPayload {

    private QuizPayloadType type;

    // CLOZE / HANGMAN
    private String mask;
    private Integer maxErrors;

    // MCQ
    private List<QuizChoice> choices;
    private Boolean allowMultiple;

    // BINARY_SWIPE
    private QuizChoice proposition;

    // ASSOCIATION / ORDERING
    private List<QuizPayloadItem> items;
    private QuizOrderingRule orderBy;

    // Layout hints for frontend rendering
    private QuizLayoutHints layoutHints;

    public QuizQuestionPayload() {
    }

    private QuizQuestionPayload(Builder b) {
        this.type = b.type;
        this.mask = b.mask;
        this.maxErrors = b.maxErrors;
        this.choices = b.choices;
        this.allowMultiple = b.allowMultiple;
        this.proposition = b.proposition;
        this.items = b.items;
        this.orderBy = b.orderBy;
        this.layoutHints = b.layoutHints;
    }

    public QuizPayloadType getType() {
        return type;
    }

    public String getMask() {
        return mask;
    }

    public Integer getMaxErrors() {
        return maxErrors;
    }

    public List<QuizChoice> getChoices() {
        return choices;
    }

    public Boolean getAllowMultiple() {
        return allowMultiple;
    }

    public QuizChoice getProposition() {
        return proposition;
    }

    public List<QuizPayloadItem> getItems() {
        return items;
    }

    public QuizOrderingRule getOrderBy() {
        return orderBy;
    }

    public QuizLayoutHints getLayoutHints() {
        return layoutHints;
    }

    public void setType(QuizPayloadType type) {
        this.type = type;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public void setMaxErrors(Integer maxErrors) {
        this.maxErrors = maxErrors;
    }

    public void setChoices(List<QuizChoice> choices) {
        this.choices = choices;
    }

    public void setAllowMultiple(Boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
    }

    public void setProposition(QuizChoice proposition) {
        this.proposition = proposition;
    }

    public void setItems(List<QuizPayloadItem> items) {
        this.items = items;
    }

    public void setOrderBy(QuizOrderingRule orderBy) {
        this.orderBy = orderBy;
    }

    public void setLayoutHints(QuizLayoutHints layoutHints) {
        this.layoutHints = layoutHints;
    }

    public static class Builder {
        private QuizPayloadType type;
        private String mask;
        private Integer maxErrors;
        private List<QuizChoice> choices;
        private Boolean allowMultiple;
        private QuizChoice proposition;
        private List<QuizPayloadItem> items;
        private QuizOrderingRule orderBy;
        private QuizLayoutHints layoutHints;

        public Builder withType(QuizPayloadType v) {
            this.type = v;
            return this;
        }

        public Builder withMask(String v) {
            this.mask = v;
            return this;
        }

        public Builder withMaxErrors(Integer v) {
            this.maxErrors = v;
            return this;
        }

        public Builder withChoices(List<QuizChoice> v) {
            this.choices = v;
            return this;
        }

        public Builder withAllowMultiple(Boolean v) {
            this.allowMultiple = v;
            return this;
        }

        public Builder withProposition(QuizChoice v) {
            this.proposition = v;
            return this;
        }

        public Builder withItems(List<QuizPayloadItem> v) {
            this.items = v;
            return this;
        }

        public Builder withOrderBy(QuizOrderingRule v) {
            this.orderBy = v;
            return this;
        }

        public Builder withLayoutHints(QuizLayoutHints v) {
            this.layoutHints = v;
            return this;
        }

        public QuizQuestionPayload build() {
            return new QuizQuestionPayload(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizQuestionPayload))
            return false;
        QuizQuestionPayload that = (QuizQuestionPayload) o;
        return type == that.type
                && Objects.equals(mask, that.mask)
                && Objects.equals(maxErrors, that.maxErrors)
                && Objects.equals(choices, that.choices)
                && Objects.equals(allowMultiple, that.allowMultiple)
                && Objects.equals(proposition, that.proposition)
                && Objects.equals(items, that.items)
                && orderBy == that.orderBy
                && Objects.equals(layoutHints, that.layoutHints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, mask, maxErrors, choices, allowMultiple, proposition, items, orderBy, layoutHints);
    }
}
