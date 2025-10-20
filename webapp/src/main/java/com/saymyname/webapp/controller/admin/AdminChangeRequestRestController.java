// src/main/java/com/saymyname/webapp/controller/admin/AdminChangeRequestRestController.java
package com.saymyname.webapp.controller.admin;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.people.ChangeRequestResolution;
import com.saymyname.service.ChangeRequestService;
import com.saymyname.service.UserService;
import com.saymyname.webapp.dto.admin.ResolveChangeRequestDto;
import com.saymyname.webapp.mapper.admin.ChangeRequestResolutionDtoMapper;

@PreAuthorize("@orgSecurity.hasRole(null, 'CLIENT_ADMIN')")
@RestController
@RequestMapping("/api/admin/change-requests")
public class AdminChangeRequestRestController {

    private final ChangeRequestService changeRequestService;
    private final UserService userService;
    private final ChangeRequestResolutionDtoMapper resolutionMapper;

    public AdminChangeRequestRestController(ChangeRequestService changeRequestService,
            UserService userService,
            ChangeRequestResolutionDtoMapper resolutionMapper) {
        this.changeRequestService = changeRequestService;
        this.userService = userService;
        this.resolutionMapper = resolutionMapper;
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<Void> resolve(
            @PathVariable("id") Long id,
            @RequestBody ResolveChangeRequestDto body,
            Principal principal) {

        User resolver = userService.getCurrentUserOrThrow(principal);

        // DTO -> Model (commande)
        ChangeRequestResolution cmd = resolutionMapper.toModel(id, resolver, body);

        // Métier
        changeRequestService.resolve(cmd);

        return ResponseEntity.noContent().build();
    }
}
