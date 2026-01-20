// src/main/java/com/saymyname/webapp/mapper/quiz/HangmanStateDtoMapper.java
package com.saymyname.webapp.mapper.quiz;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.quiz.snapshot.HangmanSnapshotState;
import com.saymyname.webapp.dto.quiz.HangmanStateDto;

/**
 * Maps HangmanSnapshotState (core model) to HangmanStateDto (API response).
 */
@Component
public class HangmanStateDtoMapper {

    public HangmanStateDto toDto(HangmanSnapshotState state) {
        if (state == null) {
            return null;
        }

        return new HangmanStateDto(
                state.getMask(),
                null, // maxErrors comes from rules, not state
                state.getErrorsCount(),
                state.getTriedLetters(),
                state.getWrongLetters());
    }

    /**
     * Enhanced version that includes maxErrors from rules.
     */
    public HangmanStateDto toDto(HangmanSnapshotState state, Integer maxErrors) {
        if (state == null) {
            return null;
        }

        return new HangmanStateDto(
                state.getMask(),
                maxErrors,
                state.getErrorsCount(),
                state.getTriedLetters(),
                state.getWrongLetters());
    }
}
