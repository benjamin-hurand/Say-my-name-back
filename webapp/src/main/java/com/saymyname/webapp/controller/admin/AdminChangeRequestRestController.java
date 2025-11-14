// src/main/java/com/saymyname/webapp/controller/admin/AdminChangeRequestRestController.java
package com.saymyname.webapp.controller.admin;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.people.BulkChangeRequestResolution;
import com.saymyname.core.model.people.ChangeRequest;
import com.saymyname.core.model.people.ChangeRequestListQuery;
import com.saymyname.core.model.people.ChangeRequestResolution;
import com.saymyname.service.ChangeRequestService;
import com.saymyname.service.UserService;
import com.saymyname.webapp.dto.admin.BulkResolveChangeRequestsDto;
import com.saymyname.webapp.dto.admin.ChangeRequestsListDto;
import com.saymyname.webapp.dto.admin.ResolveChangeRequestDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestSummaryDto;
import com.saymyname.webapp.mapper.ChangeRequestDtoMapper;
import com.saymyname.webapp.mapper.admin.ChangeRequestResolutionDtoMapper;
import com.saymyname.webapp.mapper.admin.ChangeRequestsListDtoMapper;

@PreAuthorize("@orgSecurity.hasRole(null, 'CLIENT_ADMIN')")
@RestController
@RequestMapping("/api/admin/change-requests")
public class AdminChangeRequestRestController {

    private final ChangeRequestService changeRequestService;
    private final UserService userService;
    private final ChangeRequestResolutionDtoMapper resolutionMapper;
    private final ChangeRequestsListDtoMapper listMapper;
    private final ChangeRequestDtoMapper changeRequestDtoMapper;

    public AdminChangeRequestRestController(ChangeRequestService changeRequestService,
            UserService userService,
            ChangeRequestResolutionDtoMapper resolutionMapper,
            ChangeRequestsListDtoMapper listMapper,
            ChangeRequestDtoMapper changeRequestDtoMapper) {
        this.changeRequestService = changeRequestService;
        this.userService = userService;
        this.resolutionMapper = resolutionMapper;
        this.listMapper = listMapper;
        this.changeRequestDtoMapper = changeRequestDtoMapper;
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

    @GetMapping
    public ResponseEntity<Page<ChangeRequestSummaryDto>> list(
            @ModelAttribute ChangeRequestsListDto params,
            @PageableDefault Pageable pageable) {

        ChangeRequestListQuery query = listMapper.toModel(params);
        Page<ChangeRequest> page = changeRequestService.list(query, pageable);
        return ResponseEntity.ok(changeRequestDtoMapper.toDtoPage(page));
    }

    // NEW — BULK RESOLVE
    @PostMapping("/_bulk-resolve")
    public ResponseEntity<Void> bulkResolve(
            @RequestBody BulkResolveChangeRequestsDto body,
            Principal principal) {

        User resolver = userService.getCurrentUserOrThrow(principal);
        BulkChangeRequestResolution cmd = resolutionMapper.toModel(body, resolver);
        changeRequestService.bulkResolve(cmd);
        return ResponseEntity.noContent().build();
    }
}
