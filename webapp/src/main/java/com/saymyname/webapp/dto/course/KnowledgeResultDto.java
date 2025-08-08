package com.saymyname.webapp.dto.course;

public record KnowledgeResultDto(
                Long gameModeId,
                Long personId,
                Boolean isCorrect,
                Boolean helpUsed) {

}
