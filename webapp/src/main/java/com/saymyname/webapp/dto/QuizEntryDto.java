package com.saymyname.webapp.dto;

public record QuizEntryDto (
    Long personId,
    String photoUrl,
    String initials
) {

}
