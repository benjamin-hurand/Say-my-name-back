// src/main/java/com/saymyname/webapp/controller/auth/AuthRegisterController.java
package com.saymyname.webapp.controller.auth;

import java.io.IOException;
import java.security.GeneralSecurityException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.auth.RegisterClassicResult;
import com.saymyname.core.model.auth.User;
import com.saymyname.service.RegistrationService;
import com.saymyname.service.UserService;
import com.saymyname.service.auth.RefreshTokenService;
import com.saymyname.service.email.EmailVerificationService;
import com.saymyname.webapp.dto.RegisterFormDto;
import com.saymyname.webapp.dto.RegisterGoogleDto;
import com.saymyname.webapp.dto.auth.AuthResponseDto;
import com.saymyname.webapp.dto.auth.ConfirmRegisterEmailRequestDto;
import com.saymyname.webapp.dto.auth.RegisterClassicResponseDto;
import com.saymyname.webapp.dto.auth.ResendRegisterEmailRequestDto;
import com.saymyname.webapp.mapper.AuthRegisterDtoMapper;
import com.saymyname.webapp.security.AuthCookieSupport;

@RestController
@RequestMapping("/api/auth")
public class AuthRegisterController {

    private final RegistrationService registrationService;
    private final EmailVerificationService emailVerificationService;
    private final UserService userService;

    private final RefreshTokenService refreshTokenService;
    private final AuthCookieSupport authCookieSupport;

    private final AuthResponseBuilder authResponseBuilder;
    private final AuthRegisterDtoMapper authRegisterDtoMapper;

    public AuthRegisterController(
            RegistrationService registrationService,
            EmailVerificationService emailVerificationService,
            UserService userService,
            RefreshTokenService refreshTokenService,
            AuthCookieSupport authCookieSupport,
            AuthResponseBuilder authResponseBuilder,
            AuthRegisterDtoMapper authRegisterDtoMapper) {
        this.registrationService = registrationService;
        this.emailVerificationService = emailVerificationService;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.authCookieSupport = authCookieSupport;
        this.authResponseBuilder = authResponseBuilder;
        this.authRegisterDtoMapper = authRegisterDtoMapper;
    }

    // Retourne le challenge OTP (pas de session tant que non vérifié)
    @PostMapping("/register")
    public ResponseEntity<RegisterClassicResponseDto> register(@Valid @RequestBody RegisterFormDto dto) {
        String displayName = dto.displayName() == null ? null : dto.displayName().trim();
        String email = dto.email() == null ? null : dto.email().trim();

        RegisterClassicResult result = registrationService.registerClassic(displayName, email, dto.password());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authRegisterDtoMapper.toRegisterClassicResponseDto(result.getChallenge()));
    }

    // Valide OTP -> verified_at -> renvoie AuthResponseDto + set refresh cookie +
    // set XSRF cookie
    @PostMapping("/register/confirm")
    public ResponseEntity<AuthResponseDto> confirmRegisterEmail(
            @Valid @RequestBody ConfirmRegisterEmailRequestDto dto,
            HttpServletRequest req,
            HttpServletResponse res) {

        String email = dto.email() == null ? null : dto.email().trim();
        String code = dto.code() == null ? null : dto.code().trim();

        Long userId = emailVerificationService.confirmRegisterEmailVerification(
                email,
                dto.verificationId(),
                code);

        User user = userService.findByIdemails(userId).orElseGet(() -> userService.findById(userId));

        // Issue refresh token (DB) + cookies (refresh + XSRF)
        String refreshOpaque = refreshTokenService.issueNewRefreshToken(
                user,
                headerTrim(req, "X-Device-Id"),
                headerTrim(req, "X-Device-Name"),
                clientIp(req),
                headerTrim(req, "User-Agent"));

        authCookieSupport.setRefreshAndXsrf(res, refreshOpaque);

        // Return access token + session info (JSON)
        return ResponseEntity.ok(authResponseBuilder.build(user));
    }

    @PostMapping("/register/resend")
    public ResponseEntity<RegisterClassicResponseDto> resendRegisterEmail(
            @Valid @RequestBody ResendRegisterEmailRequestDto dto) {

        String email = dto.email() == null ? null : dto.email().trim();

        var challenge = emailVerificationService.resendRegisterEmailOtp(
                email,
                dto.verificationId());

        return ResponseEntity.ok(authRegisterDtoMapper.toRegisterClassicResponseDto(challenge));
    }

    @PostMapping("/google/register")
    public ResponseEntity<AuthResponseDto> registerWithGoogle(
            @Valid @RequestBody RegisterGoogleDto dto,
            HttpServletRequest req,
            HttpServletResponse res)
            throws GeneralSecurityException, IOException {

        User user = registrationService.registerWithGoogle(dto.credential(), dto.clientId());

        String refreshOpaque = refreshTokenService.issueNewRefreshToken(
                user,
                headerTrim(req, "X-Device-Id"),
                headerTrim(req, "X-Device-Name"),
                clientIp(req),
                headerTrim(req, "User-Agent"));

        authCookieSupport.setRefreshAndXsrf(res, refreshOpaque);

        return ResponseEntity.ok(authResponseBuilder.build(user));
    }

    // -------------------- helpers --------------------

    private static String clientIp(HttpServletRequest req) {
        if (req == null)
            return null;
        String xf = req.getHeader("X-Forwarded-For");
        return (xf != null && !xf.isBlank()) ? xf.split(",")[0].trim() : req.getRemoteAddr();
    }

    private static String headerTrim(HttpServletRequest req, String name) {
        if (req == null)
            return null;
        String v = req.getHeader(name);
        if (v == null)
            return null;
        String t = v.trim();
        return t.isBlank() ? null : t;
    }
}
