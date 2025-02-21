package com.saymyname.webapp.dto;

import java.util.List;

public record ReducedGameModeDto(
    Long id,
    String operator,
    List<Long> attributeIds // Les attributs nécessaires pour les initiales
) { }