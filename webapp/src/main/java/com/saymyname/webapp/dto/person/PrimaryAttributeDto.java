package com.saymyname.webapp.dto.person;

public record PrimaryAttributeDto(
        Long personAttributeId,
        Long attributeId,
        String value,
        Integer displayOrder, // utile si tu veux piloter l’affichage côté front
        boolean primary) {
}