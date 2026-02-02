// service/quiz/candidate/CandidateService.java
package com.saymyname.service.quiz.candidate;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.exception.quiz.QuizUnprocessableException;
import com.saymyname.core.model.quiz.candidate.CandidatePool;
import com.saymyname.core.model.quiz.candidate.CandidateQuery;
import com.saymyname.persistence.dao.quiz.CandidateDao;

@Service
public class CandidateService {

    private final CandidateDao dao;

    public CandidateService(CandidateDao dao) {
        this.dao = Objects.requireNonNull(dao, "dao");
    }

    @Transactional(readOnly = true)
    public CandidatePool loadPoolOrThrow(CandidateQuery query) {
        List<Long> ids = dao.findPersonIds(query);
        if (ids == null || ids.isEmpty()) {
            throw new QuizUnprocessableException(
                    QuizUnprocessableException.ErrorCode.NO_CANDIDATE,
                    "No candidate available for current constraints");
        }
        return new CandidatePool(ids);
    }
}
