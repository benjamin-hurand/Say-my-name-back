// src/main/java/com/saymyname/webapp/dto/changerequest/AttributePreviewDto.java
package com.saymyname.webapp.dto.changerequest;

import java.util.List;

import com.saymyname.webapp.dto.person.FactMinimalDto;

public record AttributePreviewDto(
        // Valeurs actives à l’ancre “début de saison”, avant CR
        List<FactMinimalDto> baselineFutureValues,
        // Valeurs si TOUTE la CR est approuvée
        List<String> finalIfApproved) {
}
