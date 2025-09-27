package com.saymyname.webapp.dto.person;

public record SortDirectiveDto(
        String kind, // "ATTRIBUTE" | "FIELD"
        Long attributeId, // requis si kind=ATTRIBUTE
        String field, // ex: "createdAt" | "displayName" (si implémenté plus tard)
        String direction // "ASC" | "DESC"
) {
}
