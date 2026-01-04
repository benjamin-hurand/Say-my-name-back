// src/main/java/com/saymyname/webapp/controller/admin/MemberAdminController.java
package com.saymyname.webapp.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.organization.OrgMemberRow;
import com.saymyname.service.UserOrganizationService;
import com.saymyname.service.UserOrganizationService.TransferOwnershipResult;
import com.saymyname.service.UserService;
import com.saymyname.webapp.dto.organization.ChangeRoleRequest;
import com.saymyname.webapp.dto.organization.OrgMemberRowDto;
import com.saymyname.webapp.dto.organization.TransferOwnershipRequest;
import com.saymyname.webapp.dto.organization.TransferOwnershipResponse;
import com.saymyname.webapp.mapper.organization.OrgMemberRowDtoMapper;

@RestController
@RequestMapping("/api/admin/members")
public class MemberAdminController {

    private final UserOrganizationService userOrganizationService;
    private final OrgMemberRowDtoMapper mapper;
    private final UserService userService;

    public MemberAdminController(
            UserOrganizationService userOrganizationService,
            OrgMemberRowDtoMapper mapper,
            UserService userService) {
        this.userOrganizationService = userOrganizationService;
        this.mapper = mapper;
        this.userService = userService;
    }

    /**
     * Liste les membres pour l'organisation courante (OrgContext).
     *
     * Paramètre optionnel "search" pour filtrer côté serveur
     * sur le nom affiché, l'e-mail ou le label de la personne liée.
     */
    @GetMapping
    public List<OrgMemberRowDto> listMembers(
            @RequestParam(name = "search", required = false) String search) {

        List<OrgMemberRow> rows = userOrganizationService.listMembersForCurrentOrg();

        return rows.stream()
                .filter(row -> {
                    if (search == null || search.isBlank()) {
                        return true;
                    }
                    String s = search.toLowerCase();

                    boolean matchName = row.getDisplayName() != null
                            && row.getDisplayName().toLowerCase().contains(s);

                    boolean matchEmail = row.getEmail() != null
                            && row.getEmail().toLowerCase().contains(s);

                    boolean matchPerson = row.getPersonLabel() != null
                            && row.getPersonLabel().toLowerCase().contains(s);

                    return matchName || matchEmail || matchPerson;
                })
                .map(mapper::toDto)
                .toList();
    }

    /**
     * Change le rôle d’un membre dans l’orga courante.
     *
     * Règles:
     * - OWNER: peut changer le rôle de MEMBER/VIEWER/ADMIN (sauf OWNER via ce
     * endpoint).
     * - ADMIN: peut seulement changer le rôle de MEMBER/VIEWER (et ne peut pas
     * toucher ADMIN/OWNER).
     * - Interdit de promouvoir à OWNER via cet endpoint (ownership via endpoint
     * dédié).
     */
    @PatchMapping("/{targetUserId}/role")
    public OrgMemberRowDto changeRole(
            @PathVariable("targetUserId") Long targetUserId,
            @RequestBody ChangeRoleRequest request) {

        Long actorUserId = userService.getCurrentIdOrThrow();

        OrgMemberRow updated = userOrganizationService.changeRole(actorUserId, targetUserId, request.role());
        return mapper.toDto(updated);
    }

    /**
     * Retire (kick) un membre de l’orga courante.
     *
     * Règles:
     * - OWNER: peut retirer MEMBER/VIEWER/ADMIN (pas un OWNER via ce endpoint).
     * - ADMIN: peut retirer MEMBER/VIEWER uniquement.
     * - Interdit de se retirer soi-même ici (préférer endpoint "leave org" côté
     * user).
     */
    @DeleteMapping("/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable("targetUserId") Long targetUserId) {
        Long actorUserId = userService.getCurrentIdOrThrow();
        userOrganizationService.removeMember(actorUserId, targetUserId);
    }

    /**
     * Transfert d’ownership :
     * - Actor doit être OWNER
     * - Target doit être membre de l’orga
     * - Résultat: target -> OWNER, actor -> ADMIN (choix produit standard)
     */
    @PostMapping("/transfer-ownership")
    public TransferOwnershipResponse transferOwnership(@RequestBody TransferOwnershipRequest request) {
        Long actorUserId = userService.getCurrentIdOrThrow();

        TransferOwnershipResult result = userOrganizationService.transferOwnership(
                actorUserId,
                request.newOwnerUserId());

        return new TransferOwnershipResponse(
                mapper.toDto(result.oldOwnerRow()),
                mapper.toDto(result.newOwnerRow()));
    }

}
