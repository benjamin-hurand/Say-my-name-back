package com.saymyname.webapp.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.saymyname.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthUsernameController {

    private final UserService userService;

    public AuthUsernameController(UserService userService) {
        this.userService = userService;
    }

    // ——— USERNAMES ————————————————————————————————
    @GetMapping("/usernames/generate/{lang}")
    public ResponseEntity<String> generateUsername(@PathVariable String lang) {
        return ResponseEntity.ok(userService.generateUniqueUsername(lang));
    }

    @GetMapping("/usernames/isavailable/{username}")
    public ResponseEntity<Boolean> isUsernameAvailable(@PathVariable String username) {
        boolean available = !userService.checkIfAccountExistsWithUsername(username);
        return available
                ? ResponseEntity.ok(true)
                : ResponseEntity.status(HttpStatus.CONFLICT).body(false);
    }
}
