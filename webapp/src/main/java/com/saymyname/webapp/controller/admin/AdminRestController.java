// src/main/java/com/saymyname/webapp/controller/admin/AdminRestController.java
package com.saymyname.webapp.controller.admin;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.Fact;
import com.saymyname.core.model.persondirectory.AdminPersonCard;
import com.saymyname.core.model.persondirectory.AdminPersonSearchCriteria;
import com.saymyname.service.AttributeService;
import com.saymyname.service.ChangeRequestService;
import com.saymyname.service.FactService;
import com.saymyname.service.UserService;
import com.saymyname.service.person.PersonEmailService;
import com.saymyname.service.person.PersonService;
import com.saymyname.service.tenant.TenantMembershipService;
import com.saymyname.webapp.dto.FactDto;
import com.saymyname.webapp.dto.PersonDto;
import com.saymyname.webapp.dto.UserDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestSummaryDto;
import com.saymyname.webapp.dto.person.AdminPersonCardDto;
import com.saymyname.webapp.dto.person.AdminPersonDetailsDto;
import com.saymyname.webapp.dto.person.AdminPersonSearchRequestDto;
import com.saymyname.webapp.dto.person.PersonEmailDto;
import com.saymyname.webapp.dto.profile.AttributeValuesResponseDto;
import com.saymyname.webapp.dto.profile.BulkFactRequest;
import com.saymyname.webapp.mapper.BulkFactDtoMapper;
import com.saymyname.webapp.mapper.ChangeRequestDtoMapper;
import com.saymyname.webapp.mapper.FactDtoMapper;
import com.saymyname.webapp.mapper.PersonDtoMapper;
import com.saymyname.webapp.mapper.UserDtoMapper;
import com.saymyname.webapp.mapper.person.PersonDirectoryDtoMapper;
import com.saymyname.webapp.mapper.person.PersonEmailDtoMapper;

@PreAuthorize("@orgSecurity.hasRole(null, 'ADMIN') or @orgSecurity.hasRole(null, 'OWNER')")
@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final PersonService personService;
    private final AttributeService attributeService;

    private final PersonDirectoryDtoMapper personDirectoryDtoMapper;
    private final BulkFactDtoMapper bulkFactDtoMapper;

    private final FactService factService;
    private final FactDtoMapper factDtoMapper;

    private final PersonEmailService personEmailService;
    private final PersonEmailDtoMapper personEmailDtoMapper;

    private final PersonDtoMapper personDtoMapper;

    private final ChangeRequestService changeRequestService;
    private final ChangeRequestDtoMapper changeRequestDtoMapper;

    private final TenantMembershipService tenantMembershipService;

    private final UserService userService;
    private final UserDtoMapper userDtoMapper;

    private final Logger logger = LoggerFactory.getLogger(AdminRestController.class);

    public AdminRestController(
            PersonService personService,
            AttributeService attributeService,
            PersonDirectoryDtoMapper personDirectoryDtoMapper,
            BulkFactDtoMapper bulkFactDtoMapper,
            FactService factService,
            FactDtoMapper factDtoMapper,
            PersonEmailService personEmailService,
            PersonEmailDtoMapper personEmailDtoMapper,
            PersonDtoMapper personDtoMapper,
            ChangeRequestService changeRequestService,
            ChangeRequestDtoMapper changeRequestDtoMapper,
            TenantMembershipService tenantMembershipService,
            UserService userService,
            UserDtoMapper userDtoMapper) {

        this.personService = personService;
        this.attributeService = attributeService;

        this.personDirectoryDtoMapper = personDirectoryDtoMapper;
        this.bulkFactDtoMapper = bulkFactDtoMapper;

        this.factService = factService;
        this.factDtoMapper = factDtoMapper;

        this.personEmailService = personEmailService;
        this.personEmailDtoMapper = personEmailDtoMapper;

        this.personDtoMapper = personDtoMapper;

        this.changeRequestService = changeRequestService;
        this.changeRequestDtoMapper = changeRequestDtoMapper;

        this.tenantMembershipService = tenantMembershipService;

        this.userService = userService;
        this.userDtoMapper = userDtoMapper;
    }

    // === 1) Dashboard KPIs ===
    @GetMapping("/kpis")
    public Map<String, Long> getKpis() {
        long persons = personService.countAll();
        long attributes = attributeService.countAll();
        return Map.of(
                "persons", persons,
                "attributes", attributes);
    }

    // === 2) Persons — recherche filtrée/triée/paginée (admin)
    @PostMapping("/persons/search")
    public ResponseEntity<Page<AdminPersonCardDto>> searchPersonsForAdmin(
            @RequestBody AdminPersonSearchRequestDto body,
            Pageable pageable) {

        AdminPersonSearchCriteria criteria = personDirectoryDtoMapper.toAdminModel(body);
        logger.info("Admin person search criteria: {}", criteria);
        logger.debug("Admin person search criteria: {}", criteria);

        Page<AdminPersonCard> page = personService.searchPersonsForAdmin(criteria, pageable);
        Page<AdminPersonCardDto> dtoPage = page.map(personDirectoryDtoMapper::toDto);

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/persons/{id}/details")
    public ResponseEntity<AdminPersonDetailsDto> getAdminPersonDetails(
            @PathVariable("id") Long personId,
            @RequestParam(name = "includeChangeRequests", defaultValue = "false") boolean includeChangeRequests,
            @RequestParam(name = "includeFuture", defaultValue = "true") boolean includeFuture // compat front
    ) {
        // 1) Person + graph (attributs, photos)
        Person person = personService.getPersonByIdWithAllAttributes(personId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Person not found"));

        // 1bis) DTO Person (sans user dedans maintenant)
        PersonDto personDto = personDtoMapper.toDto(person);

        // 1ter) Si une entrée tenant_memberships existe (org courante) pour cette
        // Person :
        // on récupère le User + emails et on le mappe en UserDto (avec orgRole)
        UserDto userDto = null;

        Optional<Long> linkedUserIdOpt = tenantMembershipService.findUserIdByPersonId(personId);
        if (linkedUserIdOpt.isPresent()) {
            Long userId = linkedUserIdOpt.get();

            OrgRole orgRole = tenantMembershipService.findRoleForCurrentTenant(userId).orElse(null);

            Optional<User> userOpt = userService.findByIdWithEmails(userId);
            if (userOpt.isPresent()) {
                userDto = userDtoMapper.toDto(userOpt.get(), orgRole);
            } else {
                // Cas DB incohérent : lien uo.person_id -> user_id mais user introuvable
                logger.warn("Linked userId={} for personId={} not found (current org).", userId, personId);
            }
        }

        // 2) ChangeRequests (optionnel)
        List<ChangeRequestSummaryDto> crDtos = includeChangeRequests
                ? changeRequestService.findOpenForPerson(personId).stream()
                        .map(changeRequestDtoMapper::toSummaryDto)
                        .toList()
                : List.of();

        // 3) Emails de la personne dans l’orga courante
        List<PersonEmailDto> emailDtos = personEmailService.listByPerson(personId).stream()
                .map(personEmailDtoMapper::toResponse)
                .toList();

        // 4) Réponse
        AdminPersonDetailsDto response = new AdminPersonDetailsDto(personDto, userDto, crDtos, emailDtos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/persons/{id}")
    public Person getPerson(@PathVariable Long id) {
        return personService.findById(id).orElseThrow(
                () -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Person not found"));
    }

    /**
     * POST /api/admin/persons/{personId}/attributes/{attributeId}/bulk
     */
    @PostMapping("/persons/{personId}/attributes/{attributeId}/bulk")
    public ResponseEntity<AttributeValuesResponseDto> applyAttributeChangesForPersonAsAdmin(
            @PathVariable("personId") Long personId,
            @PathVariable("attributeId") Long attributeId,
            @RequestBody BulkFactRequest body) {

        var toCreate = bulkFactDtoMapper.toCreateModels(body.create());
        var toUpdate = bulkFactDtoMapper.toUpdateModels(body.update());
        var toDelete = bulkFactDtoMapper.toDeleteModels(body.delete());

        List<Fact> updated = factService.applyChangesForPerson(
                personId,
                attributeId,
                toCreate,
                toUpdate,
                toDelete,
                true);

        List<FactDto> updatedDtos = updated.stream()
                .map(factDtoMapper::toDto)
                .toList();

        var response = new AttributeValuesResponseDto(attributeId, updatedDtos);
        return ResponseEntity.ok(response);
    }
}
