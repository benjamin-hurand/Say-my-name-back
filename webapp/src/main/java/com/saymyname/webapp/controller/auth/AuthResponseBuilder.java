// src/main/java/com/saymyname/webapp/controller/auth/AuthResponseBuilder.java
package com.saymyname.webapp.controller.auth;

import com.saymyname.core.model.auth.User;
import com.saymyname.security.jwt.JwtService;
import com.saymyname.service.UserOrganizationService;
import com.saymyname.webapp.dto.auth.AuthResponseDto;
import com.saymyname.webapp.dto.organization.UserOrganizationDto;
import com.saymyname.webapp.mapper.organization.UserOrganizationDtoMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AuthResponseBuilder {

    private final JwtService jwtService;
    private final UserOrganizationService userOrganizationService;
    private final UserOrganizationDtoMapper userOrganizationDtoMapper;

    public AuthResponseBuilder(JwtService jwtService,
            UserOrganizationService userOrganizationService,
            UserOrganizationDtoMapper userOrganizationDtoMapper) {
        this.jwtService = jwtService;
        this.userOrganizationService = userOrganizationService;
        this.userOrganizationDtoMapper = userOrganizationDtoMapper;
    }

    public AuthResponseDto build(User user) {
        // subject = publicId (UUID) — robuste si email change
        final UUID pubId = user.getPublicId();
        final String subject = (pubId != null) ? pubId.toString() : String.valueOf(user.getId());

        String token = jwtService.generateToken(subject, user.getPasswordVersion());

        List<UserOrganizationDto> orgDtos = userOrganizationService
                .getOrganizationsForUser(user.getId())
                .stream()
                .map(userOrganizationDtoMapper::toDto)
                .toList();
        return new AuthResponseDto(
                token,
                (pubId != null ? pubId.toString() : null),
                user.getDisplayName(),
                user.isAdmin(),
                orgDtos);
    }
}
