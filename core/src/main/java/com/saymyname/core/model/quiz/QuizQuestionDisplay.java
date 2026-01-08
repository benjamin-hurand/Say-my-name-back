// src/main/java/com/saymyname/core/model/quiz/QuizQuestionDisplay.java
package com.saymyname.core.model.quiz;

import java.util.Objects;

public class QuizQuestionDisplay {
    private String prompt;
    private String subtitle;
    private String inputPlaceholder;
    private Boolean timed;
    private Integer timeLimitMs;

    public QuizQuestionDisplay() {
    }

    private QuizQuestionDisplay(Builder b) {
        this.prompt = b.prompt;
        this.subtitle = b.subtitle;
        this.inputPlaceholder = b.inputPlaceholder;
        this.timed = b.timed;
        this.timeLimitMs = b.timeLimitMs;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getInputPlaceholder() {
        return inputPlaceholder;
    }

    public Boolean getTimed() {
        return timed;
    }

    public Integer getTimeLimitMs() {
        return timeLimitMs;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setInputPlaceholder(String inputPlaceholder) {
        this.inputPlaceholder = inputPlaceholder;
    }

    public void setTimed(Boolean timed) {
        this.timed = timed;
    }

    public void setTimeLimitMs(Integer timeLimitMs) {
        this.timeLimitMs = timeLimitMs;
    }

    public static class Builder {
        private String prompt;
        private String subtitle;
        private String inputPlaceholder;
        private Boolean timed;
        private Integer timeLimitMs;

        public Builder withPrompt(String v) {
            this.prompt = v;
            return this;
        }

        public Builder withSubtitle(String v) {
            this.subtitle = v;
            return this;
        }

        public Builder withInputPlaceholder(String v) {
            this.inputPlaceholder = v;
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

        public QuizQuestionDisplay build() {
            return new QuizQuestionDisplay(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizQuestionDisplay))
            return false;
        QuizQuestionDisplay that = (QuizQuestionDisplay) o;
        return Objects.equals(prompt, that.prompt)
                && Objects.equals(subtitle, that.subtitle)
                && Objects.equals(inputPlaceholder, that.inputPlaceholder)
                && Objects.equals(timed, that.timed)
                && Objects.equals(timeLimitMs, that.timeLimitMs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prompt, subtitle, inputPlaceholder, timed, timeLimitMs);
    }
}
