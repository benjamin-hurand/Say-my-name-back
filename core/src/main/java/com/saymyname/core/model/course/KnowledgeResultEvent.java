package com.saymyname.core.model.course;

public record KnowledgeResultEvent(
        Long gameModeId,
        Long personId,
        boolean correct,
        boolean helpUsed) {
}