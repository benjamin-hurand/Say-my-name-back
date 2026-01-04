// src/main/java/com/saymyname/webapp/controller/auth/AuthEmailController.java
package com.saymyname.webapp.controller.auth;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.auth.EmailVerificationChallenge;
import com.saymyname.core.model.auth.EmailVerificationConfirmation;
import com.saymyname.core.model.auth.User;
import com.saymyname.service.UserService;
import com.saymyname.service.email.EmailVerificationService;
import com.saymyname.webapp.dto.auth.AddEmailRequestDto;
import com.saymyname.webapp.dto.auth.AddEmailResponseDto;
import com.saymyname.webapp.dto.auth.ConfirmEmailVerificationRequestDto;
import com.saymyname.webapp.dto.auth.ConfirmEmailVerificationResponseDto;
import com.saymyname.webapp.dto.auth.ResendEmailVerificationRequestDto;
import com.saymyname.webapp.mapper.AuthEmailDtoMapper;

@RestController
@RequestMapping("/api/auth")
public class AuthEmailController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final AuthEmailDtoMapper authEmailDtoMapper;

    public AuthEmailController(
            UserService userService,
            EmailVerificationService emailVerificationService,
            AuthEmailDtoMapper authEmailDtoMapper) {
        this.userService = userService;
        this.emailVerificationService = emailVerificationService;
        this.authEmailDtoMapper = authEmailDtoMapper;
    }

    @PostMapping("/emails/add")
    public ResponseEntity<AddEmailResponseDto> addEmail(@RequestBody AddEmailRequestDto dto) {
        User me = userService.getCurrentAuthenticatedUserOrThrow();

        String email = dto != null ? safeTrim(dto.email()) : null;
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        boolean makePrimaryNow = dto.makePrimaryNow() != null && dto.makePrimaryNow();

        EmailVerificationChallenge challenge = emailVerificationService.requestAddEmailVerification(me.getId(), email,
                makePrimaryNow);

        return ResponseEntity.ok(authEmailDtoMapper.toAddEmailResponseDto(challenge));
    }

    @PostMapping("/emails/confirm")
    public ResponseEntity<ConfirmEmailVerificationResponseDto> confirm(
            @RequestBody ConfirmEmailVerificationRequestDto dto) {

        User me = userService.getCurrentAuthenticatedUserOrThrow();

        String email = dto != null ? safeTrim(dto.email()) : null;
        UUID verificationId = dto != null ? dto.verificationId() : null;
        String code = dto != null ? safeTrim(dto.code()) : null;

        if (email == null || email.isBlank() || verificationId == null || code == null || code.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        EmailVerificationConfirmation confirmation = emailVerificationService.confirmAddEmailVerification(me.getId(),
                email, verificationId, code);

        return ResponseEntity.ok(authEmailDtoMapper.toConfirmEmailVerificationResponseDto(confirmation));
    }

    /**
     * Resend explicite (flow complet).
     * Note: même si tu fais déjà resend implicite via /emails/add,
     * ce endpoint est utile pour le bouton "Renvoyer le code".
     */
    @PostMapping("/emails/resend")
    public ResponseEntity<AddEmailResponseDto> resend(@RequestBody ResendEmailVerificationRequestDto dto) {
        User me = userService.getCurrentAuthenticatedUserOrThrow();

        String email = dto != null ? safeTrim(dto.email()) : null;
        UUID verificationId = dto != null ? dto.verificationId() : null;

        if (email == null || email.isBlank() || verificationId == null) {
            return ResponseEntity.badRequest().build();
        }

        EmailVerificationChallenge challenge = emailVerificationService.resendAddEmailOtp(me.getId(), email,
                verificationId);

        // On réutilise AddEmailResponseDto (car c'est “OTP challenge info”)
        return ResponseEntity.ok(authEmailDtoMapper.toAddEmailResponseDto(challenge));
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}
