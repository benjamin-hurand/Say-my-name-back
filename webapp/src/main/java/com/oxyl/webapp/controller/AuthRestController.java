package com.oxyl.webapp.controller;

import com.oxyl.core.model.common.User;
import com.oxyl.service.UserService;
import com.oxyl.webapp.config.JWTUtils;
import com.oxyl.webapp.dto.LoginDto;
import com.oxyl.webapp.dto.LoginGoogleDto;
import com.oxyl.webapp.dto.RegisterFormDto;
import com.oxyl.service.GoogleAuthService;
import com.oxyl.webapp.dto.RegisterGoogleDto;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

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

    // REGISTER ---------------------------------------------------------------------------
    @PostMapping("/auth/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterFormDto user)  {
        if(userService.checkIfAccountExistsWithEmail(user.email())) {
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

    @PostMapping("/auth/google/register")
    public ResponseEntity<Object> registerWithGoogle(@Valid @RequestBody RegisterGoogleDto user) {
        logger.info("RECU : credential: {}, clientId: {}, select_by: {}", user.credential(), user.clientId(), user.select_by());

        String credential = user.credential();
        String clientId = user.clientId();
        String selectBy = user.select_by();
        try {
            String email = googleAuthService.getEmail(credential, clientId); // todo: Exception to handle !!
            logger.info("REGISTER GOOGLE : credential: {}, clientId: {}, selectBy: {}, email: {}", credential, clientId, selectBy, email);

            if(userService.checkIfAccountExistsWithEmail(email)) {
                // Then login !
                User actualUser = userService.findByEmailOrUsername(email);
                if (isNotActive(email)) {
                    userService.setActive(actualUser);
                }
                return new ResponseEntity<>(getMessage(actualUser), HttpStatus.OK);
            }

            // Create and save the user with set verified status to false
            User newUser = new User.Builder()
                    .withEmail(email)
                    .withUsername(userService.generateUniqueUsername("french")) // todo: Can be "english" also !!
                    .withPassword(googleAuthService.generatePassword())
                    .withRoles("ROLE_USER")
                    .withActive(true)
                    .build();
            userService.save(newUser);


        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }


        // Generate jwt token
        // String verificationToken = jwtUtils.generateTokenFromUsername(newUser.getUsername());
        //
        // Send email with url with jwt inside for front reception then back management
        //
        return ResponseEntity.status(HttpStatus.CREATED).body("Registration successful.");
    }

    @GetMapping("/usernames/generate/{lang}")
    public ResponseEntity<String> generateUsername(@PathVariable(name="lang") String lang) {
        String username = userService.generateUniqueUsername(lang);
        return ResponseEntity.ok(username);
    }

    @GetMapping("/usernames/isavailable/{username}")
    public ResponseEntity<Boolean> isUsernameAvailable(@PathVariable(name="username") String username) {
        if(userService.checkIfAccountExistsWithUsername(username)) {
           return new ResponseEntity<>(false, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(true,HttpStatus.OK);
    }

    // LOGIN -----------------------------------------------------------------------------
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        // Get userDetails with identifier and password
        Authentication authenticate = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginDto.identifier(), loginDto.password()));
        UserDetails userDetails = (UserDetails) authenticate.getPrincipal();
        User actualUser = userService.findByEmailOrUsername(loginDto.identifier());
        // Checking if the account is active
        if (!actualUser.isActive()) {
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body("Email not verified. Please check your inbox for a verification link.");
        }
        return new ResponseEntity<>(getMessage(actualUser), HttpStatus.OK);
    }

    @PostMapping("/auth/google/login")
    public ResponseEntity<?> loginWithGoogle(@RequestBody LoginGoogleDto loginDto) throws Exception {
        logger.info("RECU : credential: {}, clientId: {}, select_by: {}", loginDto.credential(), loginDto.clientId(), loginDto.select_by());

        String credential = loginDto.credential();
        String clientId = loginDto.clientId();
        String email = googleAuthService.getEmail(credential, clientId);
        String selectBy = loginDto.select_by();
        logger.info("CONNEXION GOOGLE : credential: {}, clientId: {}, selectBy: {}, email: {}", credential, clientId, selectBy, email);

        User actualUser = userService.findByEmailOrUsername(email);

        if (!actualUser.isActive()) {
            userService.setActive(actualUser);
        }

        return new ResponseEntity<>(getMessage(actualUser), HttpStatus.OK);
    }
    private Map<String, Object> getMessage(User userDetails) {
        Map<String, Object> response = new HashMap<>();
        response.put("jwt", jwtUtils.generateJwtResponseEntity(userDetails).getBody());
        response.put("roles", getRoles(userDetails));
        response.put("username", userDetails.getUsername());
        response.put("email", userDetails.getEmail());
        return response;
    }

    private String getRoles(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("USER");  // Mettez une valeur par défaut si besoin
    }

    private boolean isNotActive(String identifier) {
        logger.info("isNotActive in controller : ");
        return userService.findByEmailOrUsername(identifier).isActive();
    }

}
