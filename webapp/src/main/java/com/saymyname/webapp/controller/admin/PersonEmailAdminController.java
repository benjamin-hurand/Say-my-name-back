// src/main/java/com/saymyname/webapp/controller/admin/PersonEmailAdminController.java
package com.saymyname.webapp.controller.admin;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.people.PersonEmail;
import com.saymyname.service.UserService;
import com.saymyname.service.person.PersonEmailService;
import com.saymyname.webapp.dto.person.CreatePersonEmailRequestDto;
import com.saymyname.webapp.dto.person.PersonEmailDto;
import com.saymyname.webapp.dto.person.UpdatePersonEmailRequestDto;
import com.saymyname.webapp.mapper.person.PersonEmailDtoMapper;

@RestController
@RequestMapping("/api/admin/persons/{personId}/emails")
public class PersonEmailAdminController {

    private final PersonEmailService service;
    private final UserService userService;
    private final PersonEmailDtoMapper mapper;

    public PersonEmailAdminController(PersonEmailService service,
            UserService userService,
            PersonEmailDtoMapper mapper) {
        this.service = service;
        this.userService = userService;
        this.mapper = mapper;
    }

    // --------- READS ---------

    @GetMapping
    public List<PersonEmailDto> list(@PathVariable("personId") Long personId) {
        userService.getCurrentAuthenticatedUserOrThrow();
        return mapper.toResponseList(service.listByPerson(personId));
    }

    @GetMapping("{emailId}")
    public ResponseEntity<PersonEmailDto> get(@PathVariable("personId") Long personId,
            @PathVariable("emailId") Long emailId) {
        userService.getCurrentAuthenticatedUserOrThrow();

        Optional<PersonEmail> opt = service.get(emailId);
        if (opt.isEmpty())
            return ResponseEntity.notFound().build();

        PersonEmail m = opt.get();
        Long ownerId = (m.getPerson() != null ? m.getPerson().getId() : null);
        if (ownerId == null || !ownerId.equals(personId))
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(mapper.toResponse(m));
    }

    // --------- COMMANDS ---------

    @PostMapping
    public ResponseEntity<PersonEmailDto> create(@PathVariable("personId") Long personId,
            @RequestBody CreatePersonEmailRequestDto req) {
        userService.getCurrentAuthenticatedUserOrThrow();

        PersonEmail toCreate = mapper.toModelForCreate(personId, req);
        PersonEmail saved = service.create(toCreate);

        URI location = URI.create(String.format("/api/admin/persons/%d/emails/%d", personId, saved.getId()));
        return ResponseEntity.created(location).body(mapper.toResponse(saved));
    }

    @PutMapping("{emailId}")
    public ResponseEntity<PersonEmailDto> update(@PathVariable("personId") Long personId,
            @PathVariable Long emailId,
            @RequestBody UpdatePersonEmailRequestDto req) {
        userService.getCurrentAuthenticatedUserOrThrow();

        Optional<PersonEmail> before = service.get(emailId);
        if (before.isEmpty())
            return ResponseEntity.notFound().build();

        Long ownerId = (before.get().getPerson() != null ? before.get().getPerson().getId() : null);
        if (ownerId == null || !ownerId.equals(personId))
            return ResponseEntity.notFound().build();

        PersonEmail toUpdate = mapper.toModelForUpdate(personId, emailId, req);
        PersonEmail saved = service.update(toUpdate);

        return ResponseEntity.ok(mapper.toResponse(saved));
    }

    @DeleteMapping("{emailId}")
    public ResponseEntity<Void> delete(@PathVariable("personId") Long personId,
            @PathVariable("emailId") Long emailId) {
        userService.getCurrentAuthenticatedUserOrThrow();

        Optional<PersonEmail> before = service.get(emailId);
        if (before.isEmpty())
            return ResponseEntity.noContent().build();

        Long ownerId = (before.get().getPerson() != null ? before.get().getPerson().getId() : null);
        if (ownerId == null || !ownerId.equals(personId))
            return ResponseEntity.noContent().build();

        service.delete(emailId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("{emailId}/primary")
    public ResponseEntity<Void> setPrimary(@PathVariable("personId") Long personId,
            @PathVariable("emailId") Long emailId) {
        userService.getCurrentAuthenticatedUserOrThrow();

        Optional<PersonEmail> before = service.get(emailId);
        if (before.isEmpty())
            return ResponseEntity.notFound().build();

        Long ownerId = (before.get().getPerson() != null ? before.get().getPerson().getId() : null);
        if (ownerId == null || !ownerId.equals(personId))
            return ResponseEntity.notFound().build();

        service.setPrimary(personId, emailId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("{emailId}/verify")
    public ResponseEntity<Void> markVerified(@PathVariable("personId") Long personId,
            @PathVariable("emailId") Long emailId) {
        userService.getCurrentAuthenticatedUserOrThrow();

        Optional<PersonEmail> before = service.get(emailId);
        if (before.isEmpty())
            return ResponseEntity.notFound().build();

        Long ownerId = (before.get().getPerson() != null ? before.get().getPerson().getId() : null);
        if (ownerId == null || !ownerId.equals(personId))
            return ResponseEntity.notFound().build();

        service.markVerified(emailId, null);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("{emailId}/bounce")
    public ResponseEntity<Void> markBounced(@PathVariable("personId") Long personId,
            @PathVariable("emailId") Long emailId) {
        userService.getCurrentAuthenticatedUserOrThrow();

        Optional<PersonEmail> before = service.get(emailId);
        if (before.isEmpty())
            return ResponseEntity.notFound().build();

        Long ownerId = (before.get().getPerson() != null ? before.get().getPerson().getId() : null);
        if (ownerId == null || !ownerId.equals(personId))
            return ResponseEntity.notFound().build();

        service.markBounced(emailId, null);
        return ResponseEntity.noContent().build();
    }
}
