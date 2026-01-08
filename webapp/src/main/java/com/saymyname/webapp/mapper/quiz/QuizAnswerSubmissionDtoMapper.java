// src/main/java/com/saymyname/webapp/mapper/quiz/QuizAnswerSubmissionDtoMapper.java
package com.saymyname.webapp.mapper.quiz;

import java.util.List;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizAssociationPair;
import com.saymyname.webapp.dto.quiz.QuizAnswerSubmissionDto;
import com.saymyname.webapp.dto.quiz.QuizAssociationPairDto;

@Component
public class QuizAnswerSubmissionDtoMapper {

    public QuizAnswerSubmission toModel(QuizAnswerSubmissionDto dto) {
        if (dto == null)
            return null;

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

        return s;
    }

    private QuizAssociationPair toPair(QuizAssociationPairDto dto) {
        QuizAssociationPair p = new QuizAssociationPair();
        p.setLeftId(dto.leftId());
        p.setRightId(dto.rightId());
        return p;
    }

    private static <T> List<T> safe(List<T> v) {
        return v == null ? List.of() : v;
    }
}
