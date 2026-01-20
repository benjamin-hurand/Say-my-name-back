// src/main/java/com/saymyname/service/quiz/handle/AttemptRef.java
package com.saymyname.service.quiz.handle;

import java.util.Objects;

import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.service.quiz.store.QuizAttemptStore.AttemptHandle;

public record AttemptRef(
        QuizQuestionSource source,
        AttemptHandle handle) {
    public AttemptRef {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(handle, "handle");
    }
}
