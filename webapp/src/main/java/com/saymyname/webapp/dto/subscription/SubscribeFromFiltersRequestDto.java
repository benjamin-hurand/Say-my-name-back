// src/main/java/com/saymyname/webapp/dto/subscription/SubscribeFromFiltersRequestDto.java
package com.saymyname.webapp.dto.subscription;

import com.saymyname.webapp.dto.challenge.ChallengeMenuDto;

/**
 * Utilise ta grammaire de filtres existante pour sélectionner des personnes.
 */
public record SubscribeFromFiltersRequestDto(ChallengeMenuDto filters) {
}
