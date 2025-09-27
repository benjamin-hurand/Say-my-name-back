package com.saymyname.webapp.dto.person;

import java.util.List;

public record AttributeFilterDto(
        Long attributeId,
        String operator, // "IN" | "LIKE" | "RANGE"
        List<String> values // pour IN / LIKE ; pour RANGE: [min, max] (date/numérique)
) {
}
