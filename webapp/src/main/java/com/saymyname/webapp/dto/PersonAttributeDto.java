package com.saymyname.webapp.dto;

import java.time.LocalDateTime;

public record PersonAttributeDto(
                Long id,
                AttributeDto attribute,
                String value,
                LocalDateTime validFrom,
                LocalDateTime validTo,
                boolean pendingDelete) {
}
