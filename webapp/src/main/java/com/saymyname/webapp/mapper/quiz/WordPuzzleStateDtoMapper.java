// src/main/java/com/saymyname/webapp/mapper/quiz/WordPuzzleStateDtoMapper.java
package com.saymyname.webapp.mapper.quiz;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.quiz.snapshot.WordPuzzleSnapshotState;
import com.saymyname.webapp.dto.quiz.WordPuzzleStateDto;

/**
 * Maps WordPuzzleSnapshotState (core model) to WordPuzzleStateDto (API
 * response).
 */
@Component
public class WordPuzzleStateDtoMapper {

    public WordPuzzleStateDto toDto(WordPuzzleSnapshotState state) {
        if (state == null) {
            return null;
        }

        List<WordPuzzleStateDto.AttemptDto> attemptDtos = state.getAttempts().stream()
                .map(this::toAttemptDto)
                .collect(Collectors.toList());

        return new WordPuzzleStateDto(
                attemptDtos,
                state.getAttemptsRemaining(),
                state.getSolved(),
                state.getFailed());
    }

    private WordPuzzleStateDto.AttemptDto toAttemptDto(WordPuzzleSnapshotState.AttemptEntry attempt) {
        return new WordPuzzleStateDto.AttemptDto(
                attempt.getWord(),
                attempt.getFeedback(),
                attempt.getPosition());
    }
}
