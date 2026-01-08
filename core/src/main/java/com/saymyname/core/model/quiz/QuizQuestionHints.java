// src/main/java/com/saymyname/core/model/quiz/QuizQuestionHints.java
package com.saymyname.core.model.quiz;

import java.util.Objects;

public class QuizQuestionHints {
    private String initials;

    public QuizQuestionHints() {
    }

    public QuizQuestionHints(String initials) {
        this.initials = initials;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizQuestionHints))
            return false;
        return Objects.equals(initials, ((QuizQuestionHints) o).initials);
    }

    @Override
    public int hashCode() {
        return Objects.hash(initials);
    }
}
