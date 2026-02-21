package com.saymyname.webapp.dto;

import java.time.LocalDateTime;

public record FactDto(
                Long id,
                Long attributeId,
                String value,
                LocalDateTime validFrom,
                LocalDateTime validTo,
                boolean pendingDelete) {
}
