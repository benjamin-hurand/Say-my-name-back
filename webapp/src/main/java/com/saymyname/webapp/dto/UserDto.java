// src/main/java/com/saymyname/webapp/dto/UserDto.java
package com.saymyname.webapp.dto;

import java.util.List;

import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.enums.SrsAlgorithm;

public record UserDto(
        String publicId,
        String displayName,
        String primaryEmail, // ← remplace l’ancien email
        List<UserEmailDto> emails, // ← toutes les adresses du compte
        SrsAlgorithm srsAlgorithm,
        String roles,
        boolean active,
        OrgRole organizationRole) {
}
