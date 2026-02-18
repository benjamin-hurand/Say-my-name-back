// src/main/java/com/saymyname/webapp/controller/admin/InvitationAdminController.java
package com.saymyname.webapp.controller.admin;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.invitation.Invitation;
import com.saymyname.service.UserService;
import com.saymyname.service.invitation.InvitationService;
import com.saymyname.webapp.dto.invitation.CreateInvitationRequestDto;
import com.saymyname.webapp.dto.invitation.InvitationDto;
import com.saymyname.webapp.mapper.invitation.InvitationDtoMapper;

@RestController
@RequestMapping("/api/admin/invitations")
public class InvitationAdminController {

    private final InvitationService service;
    private final InvitationDtoMapper mapper;
    private final UserService userService;

    public InvitationAdminController(InvitationService service,
            InvitationDtoMapper mapper,
            UserService userService) {
        this.service = service;
        this.mapper = mapper;
        this.userService = userService;
    }

    // -------- READS --------

    /** Liste des invitations pour l'organisation courante (TenantContext). */
    @GetMapping
    public List<InvitationDto> list() {
        return service.list().stream()
                .map(mapper::toDto) // projection légère
                .toList();
    }

    /** Lecture d’une invitation. */
    @GetMapping("{id}")
    public ResponseEntity<InvitationDto> get(@PathVariable("id") Long id) {
        return service.get(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // -------- COMMANDS --------

    /** Création et envoi d’une invitation. */
    @PostMapping
    public ResponseEntity<InvitationDto> createAndSend(@RequestBody CreateInvitationRequestDto req) {
        // Auth (utile pour tracer createdBy)
        User current = userService.getCurrentAuthenticatedUserOrThrow();

        // Validation simple (le service revalide aussi)
        if (req.rawToken() == null || req.rawToken().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // DTO -> Model (hashs gérés par le service)
        Invitation model = mapper.toModel(req);

        Invitation saved = service.create(
                model,
                current,
                req.rawToken(),
                req.rawPin());

        InvitationDto body = mapper.toDto(saved);
        URI location = URI.create(String.format("/api/admin/invitations/%d", saved.getId()));
        return ResponseEntity.created(location).body(body);
    }

    /** Suppression d’une invitation. */
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Révocation d’une invitation. */
    @PostMapping("{id}/revoke")
    public ResponseEntity<InvitationDto> revoke(@PathVariable("id") Long id) {
        // Auth (si besoin de tracer l’auteur de la révocation)
        userService.getCurrentAuthenticatedUserOrThrow();

        Invitation inv = service.revoke(id, null);
        return ResponseEntity.ok(mapper.toDto(inv));
    }
}
