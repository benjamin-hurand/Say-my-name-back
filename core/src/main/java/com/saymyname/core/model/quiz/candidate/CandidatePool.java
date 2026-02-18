package com.saymyname.core.model.quiz.candidate;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CandidatePool {
    @Builder.Default
    List<Long> personIds = List.of();

    public int size() {
        return personIds.size();
    }

    public boolean isEmpty() {
        return personIds.isEmpty();
    }
}
