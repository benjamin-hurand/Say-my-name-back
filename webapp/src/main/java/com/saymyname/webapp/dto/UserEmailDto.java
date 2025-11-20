// src/main/java/com/saymyname/webapp/dto/UserEmailDto.java
package com.saymyname.webapp.dto;

public record UserEmailDto(
        Long id,
        String email,
        boolean primary,
        boolean loginAllowed,
        boolean recoveryAllowed,
        String verifiedAt, // ISO-8601 ou null
        String addedAt, // ISO-8601 ou null
        String recoveryEligibleAt, // ISO-8601 ou null
        String updatedAt // ISO-8601 ou null
) {
}
