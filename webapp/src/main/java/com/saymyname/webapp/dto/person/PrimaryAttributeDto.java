package com.saymyname.webapp.dto.person;

public record PrimaryAttributeDto(
                Long factId,
                Long attributeId,
                String value,
                Integer displayOrder, // utile si tu veux piloter l’affichage côté front
                boolean primary) {
}