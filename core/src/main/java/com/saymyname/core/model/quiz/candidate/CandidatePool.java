// core/model/quiz/candidate/CandidatePool.java
package com.saymyname.core.model.quiz.candidate;

import java.util.List;

public class CandidatePool {
    private final List<Long> personIds;

    public CandidatePool(List<Long> personIds) {
        this.personIds = (personIds == null) ? List.of() : List.copyOf(personIds);
    }

    public List<Long> getPersonIds() {
        return personIds;
    }

    public int size() {
        return personIds.size();
    }

    public boolean isEmpty() {
        return personIds.isEmpty();
    }
}
