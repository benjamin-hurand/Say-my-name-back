// src/main/java/com/saymyname/webapp/controller/admin/AdminEmailLookupController.java
package com.saymyname.webapp.controller.admin;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.people.PersonEmail;
import com.saymyname.service.UserService;
import com.saymyname.service.person.PersonEmailService;

@RestController
@RequestMapping("/api/admin/emails")
public class AdminEmailLookupController {

    private final PersonEmailService emailService;
    private final UserService userService;

    public AdminEmailLookupController(PersonEmailService emailService, UserService userService) {
        this.emailService = emailService;
        this.userService = userService;
    }

    public record ExistsResponse(boolean exists, Long personId) {
    }

    @GetMapping("/exists")
    public ResponseEntity<ExistsResponse> exists(@RequestParam("email") String email) {
        userService.getCurrentAuthenticatedUserOrThrow();

        Optional<PersonEmail> first = emailService.findFirstActiveByEmailIgnoreCase(email);
        return ResponseEntity.ok(new ExistsResponse(first.isPresent(),
                first.map(pe -> pe.getPerson() != null ? pe.getPerson().getId() : null).orElse(null)));
    }
}
