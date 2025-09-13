package com.saymyname.webapp.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.auth.User; // ← ajuste si besoin
import com.saymyname.service.PasswordService;
import com.saymyname.service.UserService;
import com.saymyname.webapp.dto.auth.ChangePasswordRequestDto;
import com.saymyname.webapp.dto.auth.ForgotPasswordRequestDto;
import com.saymyname.webapp.dto.auth.ResetPasswordRequestDto;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthPasswordController {

    private final PasswordService passwordService;
    private final UserService userService;

    public AuthPasswordController(PasswordService passwordService, UserService userService) {
        this.passwordService = passwordService;
        this.userService = userService;
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

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequestDto dto) {
        User user = userService.getCurrentAuthenticatedUserOrThrow();
        passwordService.changePassword(user, dto.currentPassword(), dto.newPassword());
        return ResponseEntity.noContent().build();
    }
}
