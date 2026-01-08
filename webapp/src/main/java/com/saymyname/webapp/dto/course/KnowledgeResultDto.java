// src/main/java/com/saymyname/webapp/dto/course/KnowledgeResultDto.java
package com.saymyname.webapp.dto.course;

public record KnowledgeResultDto(
        Long knowledgeId,
        Long gameModeId,
        Long personId,
        boolean isCorrect,
        boolean helpUsed,
        Long courseId,
        Long courseQuestionHistoryId,
        Integer questionRound) {
}
