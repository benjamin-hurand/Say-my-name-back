// src/main/java/com/saymyname/webapp/mapper/quiz/QuizAnswerSubmissionDtoMapper.java
package com.saymyname.webapp.mapper.quiz;

import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.quiz.HangmanAction;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizAssociationPair;
import com.saymyname.webapp.dto.quiz.HangmanSubmissionDto;
import com.saymyname.webapp.dto.quiz.QuizAnswerSubmissionDto;
import com.saymyname.webapp.dto.quiz.QuizAssociationPairDto;
import com.saymyname.webapp.dto.quiz.WordPuzzleSubmissionDto;

@Component
public class QuizAnswerSubmissionDtoMapper {

    public QuizAnswerSubmission toModel(QuizAnswerSubmissionDto dto) {
        if (dto == null) {
            return null;
        }

        QuizAnswerSubmission s = new QuizAnswerSubmission();
        s.setUserAnswer(dto.userAnswer());
        s.setSelectedChoiceId(dto.selectedChoiceId());
        s.setSelectedChoiceIds(safe(dto.selectedChoiceIds()));
        s.setSwipeRight(dto.swipeRight());
        s.setOrderingIds(safe(dto.orderingIds()));
        s.setTimeMs(dto.timeMs());

        if (dto.pairs() != null) {
            List<QuizAssociationPair> pairs = dto.pairs().stream()
                    .map(this::toPair)
                    .toList();
            s.setPairs(pairs);
        }

        if (dto.hangman() != null) {
            s.setHangman(toHangman(dto.hangman()));
        }

        if (dto.wordPuzzle() != null) {
            s.setWordPuzzle(toWordPuzzle(dto.wordPuzzle()));
        }

        return s;
    }

    private QuizAssociationPair toPair(QuizAssociationPairDto dto) {
        QuizAssociationPair p = new QuizAssociationPair();
        p.setLeftId(dto.leftId());
        p.setRightId(dto.rightId());
        return p;
    }

    private QuizAnswerSubmission.HangmanSubmission toHangman(HangmanSubmissionDto dto) {
        // On accepte null action mais on validera côté plugin (fail-fast)
        HangmanAction action = dto.action();

        QuizAnswerSubmission.HangmanSubmission h = new QuizAnswerSubmission.HangmanSubmission();
        h.setAction(action);

        // Normalisation légère (trim), canon côté plugin
        h.setLetter(dto.letter() != null ? dto.letter().trim() : null);
        h.setValue(dto.value() != null ? dto.value().trim() : null);
        return h;
    }

    private QuizAnswerSubmission.WordPuzzleSubmission toWordPuzzle(WordPuzzleSubmissionDto dto) {
        QuizAnswerSubmission.WordPuzzleSubmission w = new QuizAnswerSubmission.WordPuzzleSubmission();

        // Normalisation légère (trim), canonicalisation côté plugin
        w.setWord(dto.word() != null ? dto.word().trim() : null);
        return w;
    }

    private static <T> List<T> safe(List<T> v) {
        return v == null ? List.of() : v;
    }
}
