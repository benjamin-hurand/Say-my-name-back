package com.saymyname.webapp.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saymyname.service.PasswordService;
import com.saymyname.webapp.dto.auth.ForgotPasswordRequestDto;
import com.saymyname.webapp.dto.auth.ResetPasswordRequestDto;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthPasswordController {

    private final PasswordService passwordService;

    public AuthPasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequestDto dto,
            HttpServletRequest http) {
        if (dto != null && dto.email() != null && !dto.email().isBlank()) {
            passwordService.issueResetToken(dto.email().trim(), http);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequestDto dto) {
        passwordService.resetWithToken(dto.token(), dto.newPassword());
        return ResponseEntity.noContent().build();
    }

}
