// src/main/java/com/saymyname/service/quiz/AnswerKeyService.java
package com.saymyname.service.quiz;

import java.util.List;

public interface AnswerKeyService {
    AnswerKey compute(Long personId, List<Long> targetAttributeIds, String operator);

    record AnswerKey(
            boolean operatorAnd,
            List<String> correctValues,
            String correctAnswerJoined) {
    }
}
