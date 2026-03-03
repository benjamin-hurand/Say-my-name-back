// src/main/java/com/saymyname/service/quiz/AnswerKeyService.java
package com.saymyname.service.quiz;

public interface AnswerKeyService {

    String compute(Long personId, Long targetAttributeId);
}