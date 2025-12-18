// src/main/java/com/saymyname/webapp/dto/person/CreatePersonEmailRequest.java
package com.saymyname.webapp.dto.person;

import com.saymyname.core.model.enums.EmailKind;
import com.saymyname.core.model.enums.EmailSourceKind;

public record CreatePersonEmailRequestDto(
        String email,
        EmailKind kind, // ex: WORK | PERSONAL | OTHER
        EmailSourceKind sourceKind, // ex: IMPORT | ADMIN | SELF | SYNC
        String sourceLabel, // ex: "Import RH 2025-03", "Ajout manuel"
        Boolean primary) {
}
