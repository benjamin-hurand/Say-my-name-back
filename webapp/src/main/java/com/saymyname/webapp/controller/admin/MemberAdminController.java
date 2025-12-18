package com.saymyname.webapp.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saymyname.core.model.organization.OrgMemberRow;
import com.saymyname.service.UserOrganizationService;
import com.saymyname.webapp.dto.organization.OrgMemberRowDto;
import com.saymyname.webapp.mapper.organization.OrgMemberRowDtoMapper;

@RestController
@RequestMapping("/api/admin/members")
public class MemberAdminController {

    private final UserOrganizationService userOrganizationService;
    private final OrgMemberRowDtoMapper mapper;

    public MemberAdminController(UserOrganizationService userOrganizationService,
            OrgMemberRowDtoMapper mapper) {
        this.userOrganizationService = userOrganizationService;
        this.mapper = mapper;
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
}
