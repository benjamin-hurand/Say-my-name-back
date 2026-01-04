// src/main/java/com/saymyname/service/auth/AuthInvalidationService.java
package com.saymyname.service.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.service.UserService;

@Service
public class AuthInvalidationService {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    public AuthInvalidationService(
            UserService userService,
            RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public void invalidateAllSessions(Long userId, String reason) {
        // 1) revoke refresh tokens (sessions longues)
        refreshTokenService.revokeAllForUser(userId, reason);

        // 2) bump auth_version (invalidate access tokens JWT courts)
        userService.bumpAuthVersionOrThrow(userId);
    }
}
