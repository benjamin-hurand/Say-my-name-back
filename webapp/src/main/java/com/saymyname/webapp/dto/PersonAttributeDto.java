package com.saymyname.webapp.dto;

import java.time.LocalDateTime;

public record PersonAttributeDto(
        Long id,
        Long attributeId,
        String value,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        boolean pendingDelete) {
}
