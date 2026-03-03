// src/main/java/com/saymyname/webapp/controller/invitation/InvitationPublicController.java
package com.saymyname.webapp.controller.invitation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.invitation.Invitation;
import com.saymyname.core.model.people.Person;
import com.saymyname.service.UserService;
import com.saymyname.service.invitation.InvitationService;
import com.saymyname.webapp.dto.invitation.AcceptInvitationRequest;
import com.saymyname.webapp.dto.invitation.InvitationPreviewDto;
import com.saymyname.webapp.dto.invitation.InvitationDto;
import com.saymyname.webapp.mapper.invitation.InvitationDtoMapper;

@RestController
@RequestMapping("/api/invitations")
public class InvitationPublicController {

    private final InvitationService invitationService;
    private final InvitationDtoMapper mapper;
    private final UserService userService;

    public InvitationPublicController(InvitationService invitationService,
            InvitationDtoMapper mapper,
            UserService userService) {
        this.invitationService = invitationService;
        this.mapper = mapper;
        this.userService = userService;
    }

    @GetMapping("/preview")
    public ResponseEntity<InvitationPreviewDto> preview(@RequestParam("token") String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Invitation inv = invitationService.previewByToken(token);
        return ResponseEntity.ok(mapper.toPreview(inv));
    }

    /**
     * Acceptation publique d'une invitation via token (+ PIN optionnel).
     * Requiert un utilisateur authentifié (récupéré via UserService).
     * L’tenantId de traçage est déduit côté service de l’invitation parent.
     */
    @PostMapping("/accept")
    public ResponseEntity<InvitationDto> accept(@RequestBody AcceptInvitationRequest req) {
        // Auth courante (SecurityContext)
        User current = userService.getCurrentAuthenticatedUserOrThrow();

        if (req.token() == null || req.token().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // Person éventuellement associée (ex: self-link ou admin a pré-créé la fiche)
        Person p = null;
        if (req.personId() != null) {
            p = new Person();
            p.setId(req.personId());
        }

        Invitation inv = invitationService.acceptByToken(
                req.token(),
                req.pin(),
                current,
                p);

        return ResponseEntity.ok(mapper.toDto(inv));
    }
}
