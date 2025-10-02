package com.saymyname.webapp.controller.auth;

import com.saymyname.core.model.auth.User;
import com.saymyname.security.jwt.JwtService;
import com.saymyname.service.UserOrganizationService;
import com.saymyname.webapp.dto.auth.AuthResponseDto;
import com.saymyname.webapp.dto.organization.UserOrganizationDto;
import com.saymyname.webapp.mapper.organization.UserOrganizationDtoMapper;
import org.springframework.stereotype.Component;

import java.util.List;

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
        String token = jwtService.generateToken(user.getEmail(), user.getPasswordVersion());

        List<UserOrganizationDto> orgDtos = userOrganizationService
                .getOrganizationsForUser(user.getId())
                .stream()
                .map(userOrganizationDtoMapper::toDto)
                .toList();

        return new AuthResponseDto(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles(),
                user.getSrsAlgorithm(),
                orgDtos);
    }
}
