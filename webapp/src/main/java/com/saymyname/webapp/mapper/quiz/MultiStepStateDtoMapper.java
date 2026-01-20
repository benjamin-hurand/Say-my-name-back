// src/main/java/com/saymyname/webapp/mapper/quiz/MultiStepStateDtoMapper.java
package com.saymyname.webapp.mapper.quiz;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.quiz.snapshot.HangmanSnapshotState;
import com.saymyname.core.model.quiz.snapshot.MultiStepState;
import com.saymyname.core.model.quiz.snapshot.WordPuzzleSnapshotState;
import com.saymyname.webapp.dto.quiz.MultiStepStateDto;

/**
 * Maps core MultiStepState to API MultiStepStateDto.
 * Centralizes Hangman / WordPuzzle state mapping.
 */
@Component
public class MultiStepStateDtoMapper {

    private final HangmanStateDtoMapper hangmanStateDtoMapper;
    private final WordPuzzleStateDtoMapper wordPuzzleStateDtoMapper;

    public MultiStepStateDtoMapper(
            HangmanStateDtoMapper hangmanStateDtoMapper,
            WordPuzzleStateDtoMapper wordPuzzleStateDtoMapper) {
        this.hangmanStateDtoMapper = hangmanStateDtoMapper;
        this.wordPuzzleStateDtoMapper = wordPuzzleStateDtoMapper;
    }

    /**
     * Maps a core MultiStepState to the appropriate API DTO.
     *
     * @param state HangmanSnapshotState or WordPuzzleSnapshotState
     * @return HangmanStateDto or WordPuzzleStateDto
     */
    public MultiStepStateDto toDto(MultiStepState state) {
        if (state == null) {
            return null;
        }

        if (state instanceof HangmanSnapshotState hangmanState) {
            return hangmanStateDtoMapper.toDto(hangmanState);
        }

        if (state instanceof WordPuzzleSnapshotState wordPuzzleState) {
            return wordPuzzleStateDtoMapper.toDto(wordPuzzleState);
        }

        // Should never happen if quiz plugins are correct
        throw new IllegalStateException(
                "Unsupported MultiStepState type: " + state.getClass().getName());
    }
}
