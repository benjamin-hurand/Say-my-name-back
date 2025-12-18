// src/main/java/com/saymyname/webapp/dto/person/PersonEmailResponse.java
package com.saymyname.webapp.dto.person;

import java.time.LocalDateTime;

import com.saymyname.core.model.enums.EmailKind;
import com.saymyname.core.model.enums.EmailSourceKind;

public record PersonEmailDto(
                Long id,
                Long personId,
                String email,
                EmailKind kind,
                EmailSourceKind sourceKind,
                String sourceLabel,
                boolean primary,
                boolean active,
                LocalDateTime verifiedAt,
                LocalDateTime bouncedAt,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
}
