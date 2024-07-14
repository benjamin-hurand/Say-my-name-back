package com.oxyl.webapp.controller;

import com.oxyl.core.model.User;
import com.oxyl.service.UserService;
import com.oxyl.webapp.config.JWTUtils;
import com.oxyl.webapp.dto.LoginDto;
import com.oxyl.webapp.dto.RegisterFormDto;
import com.oxyl.service.GoogleAuthService;
import com.oxyl.webapp.dto.LoginWithUsernameDto;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AuthRestController {
    private final AuthenticationManager authManager;
    private final JWTUtils jwtUtils;
    private final UserService userService;
    private static final Logger logger = LogManager.getLogger(AuthRestController.class);
    private final GoogleAuthService googleAuthService;

    public AuthRestController(AuthenticationManager authManager, JWTUtils jwtUtils, UserService userService, GoogleAuthService googleAuthService) {
        this.authManager = authManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.googleAuthService = googleAuthService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        Authentication authenticate = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginDto.identifier(), loginDto.password()));
        UserDetails user = (UserDetails) authenticate.getPrincipal();

        // Checking if the account is active
        User actualUser = userService.findByEmailOrUsername(loginDto.identifier());
        if (!actualUser.isActive()) {
            logger.error("Should be active : {}", actualUser.isActive());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email not verified. Please check your inbox for a verification link.");
        }

        String role = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("USER");  // Mettez une valeur par défaut si besoin

        Map<String, Object> response = new HashMap<>();
        response.put("jwt", jwtUtils.generateJwtResponseEntity(user).getBody());
        response.put("role", role);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterFormDto user)  {
        if(userService.checkIfEmailExists(user.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        // Create and save the user with set verified status to false
        User newUser = new User.Builder()
                .withEmail(user.email())
                .withUsername(user.username())
                .withPassword(user.password())
                .withRoles("ROLE_USER")
                .withActive(true) // TO BE CHANGED TO FALSE WHEN EMAIL VERIFICATION
                .build();
        userService.save(newUser);

        // Generate jwt token
        // String verificationToken = jwtUtils.generateTokenFromUsername(newUser.getUsername());
        //
        // Send email with url with jwt inside for front reception then back management
        //
        return ResponseEntity.status(HttpStatus.CREATED).body("Registration successful.");
    }


    @PostMapping("/auth/google/token")
    public ResponseEntity<?> verifyToken(@RequestBody Map<String, String> tokenMap) {
        String token = tokenMap.get("token");
        try {
            // Assuming the service returns the user's ID or some other identifier after verification
            //String userId = googleAuthService.verifyToken(token);
            return ResponseEntity.ok().body("User authenticated successfully with ID: " + "userId");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Invalid token: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("An error occurred while processing the token");
        }
    }

    @GetMapping("/usernames/generate/{lang}")
    public ResponseEntity<String> generateUsername(@PathVariable(name="lang") String lang) {
        String username = userService.generateUniqueUsername(lang);
        return ResponseEntity.ok(username);
    }

}
