// src/main/java/com/saymyname/webapp/mapper/quiz/QuizAnswerResultDtoMapper.java
package com.saymyname.webapp.mapper.quiz;

import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.quiz.QuizAnswerItemResult;
import com.saymyname.core.model.quiz.QuizAnswerResult;
import com.saymyname.core.model.quiz.QuizAnswerResult;
import com.saymyname.core.model.quiz.snapshot.HangmanSnapshotState;
import com.saymyname.core.model.quiz.snapshot.MultiStepState;
import com.saymyname.core.model.quiz.snapshot.WordPuzzleSnapshotState;
import com.saymyname.webapp.dto.quiz.MultiStepStateDto;
import com.saymyname.webapp.dto.quiz.QuizAnswerItemResultDto;
import com.saymyname.webapp.dto.quiz.QuizAnswerResultDto;
import com.saymyname.webapp.dto.quiz.ResultAttributeDto;
import com.saymyname.webapp.mapper.leaderboard.XpAwardDtoMapper;

@Component
public class QuizAnswerResultDtoMapper {

        private final ResultAttributeDtoMapper resultAttributeDtoMapper;
        private final QuizQuestionDtoMapper quizQuestionDtoMapper;
        private final HangmanStateDtoMapper hangmanStateDtoMapper;
        private final WordPuzzleStateDtoMapper wordPuzzleStateDtoMapper;
        private final XpAwardDtoMapper xpAwardDtoMapper;

        public QuizAnswerResultDtoMapper(
                        ResultAttributeDtoMapper resultAttributeDtoMapper,
                        QuizQuestionDtoMapper quizQuestionDtoMapper,
                        HangmanStateDtoMapper hangmanStateDtoMapper,
                        WordPuzzleStateDtoMapper wordPuzzleStateDtoMapper,
                        XpAwardDtoMapper xpAwardDtoMapper) {
                this.resultAttributeDtoMapper = resultAttributeDtoMapper;
                this.quizQuestionDtoMapper = quizQuestionDtoMapper;
                this.hangmanStateDtoMapper = hangmanStateDtoMapper;
                this.wordPuzzleStateDtoMapper = wordPuzzleStateDtoMapper;
                this.xpAwardDtoMapper = xpAwardDtoMapper;
        }

        /**
         * ✅ New: maps the common base (works for QuizAnswerResult and
         * CourseAnswerResult)
         */
        public QuizAnswerResultDto toDto(QuizAnswerResult r) {
                if (r == null) {
                        return null;
                }

                List<QuizAnswerItemResultDto> itemDtos = r.getItemResults() == null
                                ? List.of()
                                : r.getItemResults().stream().map(this::toItemDto).toList();

                MultiStepStateDto currentStateDto = mapCurrentState(r.getCurrentState());

                return new QuizAnswerResultDto(
                                r.isCorrect(),
                                r.getFeedbackMessage(),
                                r.getNextQuestion() == null ? null : quizQuestionDtoMapper.toDto(r.getNextQuestion()),
                                itemDtos,
                                r.getIsComplete(),
                                currentStateDto,
                                r.getXpAward() == null ? null : xpAwardDtoMapper.toDto(r.getXpAward()));
        }

        private MultiStepStateDto mapCurrentState(MultiStepState currentState) {
                if (currentState == null) {
                        return null;
                }
                if (currentState instanceof HangmanSnapshotState hangmanState) {
                        return hangmanStateDtoMapper.toDto(hangmanState);
                }
                if (currentState instanceof WordPuzzleSnapshotState wordPuzzleState) {
                        return wordPuzzleStateDtoMapper.toDto(wordPuzzleState);
                }
                return null;
        }

        private QuizAnswerItemResultDto toItemDto(QuizAnswerItemResult r) {
                if (r == null) {
                        return null;
                }

                List<ResultAttributeDto> attrs = r.getResultAttributes() == null
                                ? List.of()
                                : r.getResultAttributes().stream().map(resultAttributeDtoMapper::toDto).toList();

                return new QuizAnswerItemResultDto(
                                r.getPosition(),
                                r.getRole(),
                                r.getKnowledgeId(),
                                r.getPersonId(),
                                r.isCorrect(),
                                r.getUserAnswerNormalized(),
                                r.getCorrectAnswer(),
                                attrs);
        }
}
