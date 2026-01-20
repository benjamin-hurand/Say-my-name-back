// src/main/java/com/saymyname/service/quiz/handle/QuizHandleCodec.java
package com.saymyname.service.quiz.handle;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.service.quiz.store.QuizAttemptStore.AttemptHandle;

@Component
public class QuizHandleCodec {

    private static final String COURSE_PREFIX = "c_";
    private static final String TRAINING_PREFIX = "t_";
    private static final String REVIEW_PREFIX = "r_";

    public String encode(AttemptRef ref) {
        if (ref == null)
            throw new IllegalArgumentException("ref is required");

        return switch (ref.source()) {
            case COURSE -> {
                if (!(ref.handle() instanceof AttemptHandle.DbIdHandle dh)) {
                    throw new IllegalArgumentException("COURSE requires DbIdHandle");
                }
                yield COURSE_PREFIX + dh.value();
            }
            case TRAINING -> {
                if (!(ref.handle() instanceof AttemptHandle.TokenHandle th)) {
                    throw new IllegalArgumentException("TRAINING requires TokenHandle");
                }
                yield TRAINING_PREFIX + th.value();
            }
            case REVIEW -> {
                if (!(ref.handle() instanceof AttemptHandle.TokenHandle th)) {
                    throw new IllegalArgumentException("REVIEW requires TokenHandle");
                }
                yield REVIEW_PREFIX + th.value();
            }
        };
    }

    public AttemptRef decodeOrThrow(String handle) {
        if (handle == null || handle.isBlank()) {
            throw new IllegalArgumentException("handle is required");
        }

        if (handle.startsWith(COURSE_PREFIX)) {
            String raw = handle.substring(COURSE_PREFIX.length());
            long id;
            try {
                id = Long.parseLong(raw);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid COURSE attempt id in handle: " + handle);
            }
            return new AttemptRef(QuizQuestionSource.COURSE, new AttemptHandle.DbIdHandle(id));
        }

        if (handle.startsWith(TRAINING_PREFIX)) {
            String raw = handle.substring(TRAINING_PREFIX.length());
            return new AttemptRef(QuizQuestionSource.TRAINING, new AttemptHandle.TokenHandle(raw));
        }

        if (handle.startsWith(REVIEW_PREFIX)) {
            String raw = handle.substring(REVIEW_PREFIX.length());
            return new AttemptRef(QuizQuestionSource.REVIEW, new AttemptHandle.TokenHandle(raw));
        }

        throw new IllegalArgumentException("Unknown handle prefix: " + handle);
    }

    // helpers optionnels (si tu veux garder la compat temporaire)
    public Long decodeCourseAttemptIdOrThrow(String handle) {
        AttemptRef ref = decodeOrThrow(handle);
        if (ref.source() != QuizQuestionSource.COURSE) {
            throw new IllegalArgumentException("Not a COURSE handle: " + handle);
        }
        return ((AttemptHandle.DbIdHandle) ref.handle()).value();
    }

    public String decodeTokenOrThrow(String handle) {
        AttemptRef ref = decodeOrThrow(handle);
        if (ref.source() == QuizQuestionSource.COURSE) {
            throw new IllegalArgumentException("Not a token handle: " + handle);
        }
        return ((AttemptHandle.TokenHandle) ref.handle()).value();
    }
}
