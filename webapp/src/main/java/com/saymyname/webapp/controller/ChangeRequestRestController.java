// src/main/java/com/saymyname/webapp/controller/ChangeRequestRestController.java
package com.saymyname.webapp.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.people.ChangeRequest;
import com.saymyname.service.ChangeRequestService;
import com.saymyname.service.UserService;
import com.saymyname.webapp.dto.changerequest.ChangeRequestDto;
import com.saymyname.webapp.dto.changerequest.SubmitChangeRequestDto;
import com.saymyname.webapp.dto.changerequest.UpdateChangeRequestDto;
import com.saymyname.webapp.mapper.ChangeRequestDtoMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/change-requests")
public class ChangeRequestRestController {

    private final ChangeRequestService changeRequestService;
    private final ChangeRequestDtoMapper crDtoMapper;
    private final UserService userService;

    public ChangeRequestRestController(ChangeRequestService changeRequestService,
            ChangeRequestDtoMapper crDtoMapper,
            UserService userService) {
        this.changeRequestService = changeRequestService;
        this.crDtoMapper = crDtoMapper;
        this.userService = userService;
    }

    /**
     * Crée une nouvelle enveloppe.
     * Règle stricte: s'il existe déjà un CR PENDING pour (personId, requesterId,
     * attributeId)
     * => 409 CONFLICT (on ne remplace pas via cet endpoint).
     */
    @PostMapping
    public ResponseEntity<ChangeRequestDto> submit(
            @Valid @RequestBody SubmitChangeRequestDto dto,
            Principal principal) {

        User requester = userService.getCurrentUserOrThrow(principal);
        ChangeRequest model = crDtoMapper.toModel(dto, requester);

        ChangeRequest created = changeRequestService.submitStrictNew(model);
        ChangeRequestDto out = crDtoMapper.toDto(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(out);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChangeRequestDto> replaceById(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateChangeRequestDto dto,
            Principal principal) {

        User requester = userService.getCurrentUserOrThrow(principal);
        ChangeRequest source = crDtoMapper.toModel(dto, requester);

        ChangeRequest updated = changeRequestService.replaceByIdStrict(id, source);
        ChangeRequestDto out = crDtoMapper.toDto(updated);
        return ResponseEntity.ok(out);
    }

    /** Annule l’enveloppe entière (si PENDING et appartenant au requester). */
    @PostMapping("/{changeRequestId}/cancel")
    public ResponseEntity<Void> cancelEnvelope(@PathVariable("changeRequestId") Long changeRequestId,
            Principal principal) {
        User requester = userService.getCurrentUserOrThrow(principal);
        changeRequestService.cancelEnvelope(changeRequestId, requester);
        return ResponseEntity.noContent().build();
    }
}
