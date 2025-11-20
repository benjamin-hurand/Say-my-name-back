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

import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.game.options.GameMode;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.core.model.persondirectory.AdminPersonCard;
import com.saymyname.core.model.persondirectory.AdminPersonSearchCriteria;
import com.saymyname.persistence.projection.ChallengeCardProjection;
import com.saymyname.service.AttributeService;
import com.saymyname.service.ChallengeService;
import com.saymyname.service.ChangeRequestService;
import com.saymyname.service.GameModeService;
import com.saymyname.service.PersonAttributeService;
import com.saymyname.service.PersonService;
import com.saymyname.service.UserOrganizationService;
import com.saymyname.webapp.dto.PersonAttributeDto;
import com.saymyname.webapp.dto.PersonDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestSummaryDto;
import com.saymyname.webapp.dto.person.AdminPersonCardDto;
import com.saymyname.webapp.dto.person.AdminPersonDetailsDto;
import com.saymyname.webapp.dto.person.AdminPersonSearchRequestDto;
import com.saymyname.webapp.dto.profile.AttributeValuesResponseDto;
import com.saymyname.webapp.dto.profile.BulkPersonAttributeRequest;
import com.saymyname.webapp.mapper.BulkPersonAttributeDtoMapper;
import com.saymyname.webapp.mapper.ChangeRequestDtoMapper;
import com.saymyname.webapp.mapper.PersonAttributeDtoMapper;
import com.saymyname.webapp.mapper.PersonDtoMapper;
import com.saymyname.webapp.mapper.person.PersonDirectoryDtoMapper;

@PreAuthorize("@orgSecurity.hasRole(null, 'CLIENT_ADMIN')")
@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final PersonService personService;
    private final AttributeService attributeService;
    private final GameModeService gameModeService;
    private final ChallengeService challengeService;
    private final PersonDirectoryDtoMapper personDirectoryDtoMapper;
    private final BulkPersonAttributeDtoMapper bulkPersonAttributeDtoMapper;
    private final PersonAttributeService personAttributeService;
    private final PersonAttributeDtoMapper personAttributeDtoMapper;
    private final PersonDtoMapper personDtoMapper;
    private final ChangeRequestService changeRequestService;
    private final ChangeRequestDtoMapper changeRequestDtoMapper;
    private final UserOrganizationService userOrganizationService;
    private final Logger logger = LoggerFactory.getLogger(AdminRestController.class);

    public AdminRestController(PersonService personService,
            AttributeService attributeService,
            GameModeService gameModeService,
            ChallengeService challengeService, PersonDirectoryDtoMapper personDirectoryDtoMapper,
            BulkPersonAttributeDtoMapper bulkPersonAttributeDtoMapper,
            PersonAttributeService personAttributeService,
            PersonAttributeDtoMapper personAttributeDtoMapper,
            PersonDtoMapper personDtoMapper,
            ChangeRequestService changeRequestService,
            ChangeRequestDtoMapper changeRequestDtoMapper,
            UserOrganizationService userOrganizationService) {
        this.personService = personService;
        this.attributeService = attributeService;
        this.gameModeService = gameModeService;
        this.challengeService = challengeService;
        this.personDirectoryDtoMapper = personDirectoryDtoMapper;
        this.bulkPersonAttributeDtoMapper = bulkPersonAttributeDtoMapper;
        this.personAttributeService = personAttributeService;
        this.personAttributeDtoMapper = personAttributeDtoMapper;
        this.personDtoMapper = personDtoMapper;
        this.changeRequestService = changeRequestService;
        this.changeRequestDtoMapper = changeRequestDtoMapper;
        this.userOrganizationService = userOrganizationService;
    }

    // === 1) Dashboard KPIs ===
    @GetMapping("/kpis")
    public Map<String, Long> getKpis() {
        long persons = personService.countAll();
        long attributes = attributeService.countAll();
        long challenges = challengeService.countAll();
        return Map.of(
                "persons", persons,
                "attributes", attributes,
                "challenges", challenges);
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
            @RequestParam(name = "includeFuture", defaultValue = "true") boolean includeFuture // accepté pour compat
                                                                                               // front ; non nécessaire
                                                                                               // si les PA ont leurs
                                                                                               // dates
    ) {
        // 1) Person + graph (attributs, photos)
        Optional<Person> opt = personService.getPersonByIdWithAllAttributes(personId);
        Person person = opt.orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Person not found"));

        PersonDto personDto;
        if (person.getUser() != null) {
            Long userId = person.getUser().getId();
            OrgRole orgRole = userOrganizationService.findRoleForCurrentOrg(userId).orElse(null);

            personDto = personDtoMapper.toDto(person, orgRole);
        } else {
            personDto = personDtoMapper.toDto(person);
        }

        // 2) ChangeRequests (optionnel)
        List<ChangeRequestSummaryDto> crDtos = includeChangeRequests
                ? changeRequestService.findOpenForPerson(personId).stream()
                        .map(changeRequestDtoMapper::toSummaryDto)
                        .toList()
                : List.of();

        // 3) Réponse
        AdminPersonDetailsDto response = new AdminPersonDetailsDto(personDto, crDtos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/persons/{id}")
    public Person getPerson(@PathVariable Long id) {
        return personService.findById(id).orElseThrow(
                () -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Person not found"));
    }

    // === 3) Attributes ===
    @GetMapping("/attributes")
    public List<Attribute> listAttributes() {
        return attributeService.findAll();
    }

    @GetMapping("/attributes/filterable")
    public List<Attribute> listFilterableAttributes() {
        return attributeService.getFilterableAttributes();
    }

    // === 4) GameModes ===
    @GetMapping("/game-modes")
    public List<GameMode> listGameModes() {
        return gameModeService.getAllGameModes();
    }

    // === 5) Challenges (lecture simple pour l’admin) ===
    @GetMapping("/challenges")
    public List<ChallengeCardProjection> listChallenges() {
        // Pour l’instant, on passe un ChallengeMenu null → renverra tout.
        // Plus tard: exposer ChallengeMenuDto en paramètre.
        return challengeService.getChallengesList(null);
    }

    /**
     * POST /api/admin/persons/{personId}/attributes/{attributeId}/bulk
     * Applique en une fois create/update/delete sur l’attribut {attributeId} pour
     * la personne {personId}.
     * Ignore la policy RESTRICTED (admin bypass).
     * Conserve les règles de typage, required, multiplicité et déduplication.
     */
    @PostMapping("/persons/{personId}/attributes/{attributeId}/bulk")
    public ResponseEntity<AttributeValuesResponseDto> applyAttributeChangesForPersonAsAdmin(
            @PathVariable("personId") Long personId,
            @PathVariable("attributeId") Long attributeId,
            @RequestBody BulkPersonAttributeRequest body) {
        // 1) DTO -> modèles (delta)
        var toCreate = bulkPersonAttributeDtoMapper.toCreateModels(body.create());
        var toUpdate = bulkPersonAttributeDtoMapper.toUpdateModels(body.update());
        var toDelete = bulkPersonAttributeDtoMapper.toDeleteModels(body.delete());

        // 2) Orchestration via service en bypassant RESTRICTED
        List<PersonAttribute> updated = personAttributeService.applyChangesForPerson(
                personId,
                attributeId,
                toCreate,
                toUpdate,
                toDelete,
                /* bypassRestricted */ true);

        // 3) Models -> DTO
        List<PersonAttributeDto> updatedDtos = updated.stream()
                .map(personAttributeDtoMapper::toDto)
                .toList();

        var response = new AttributeValuesResponseDto(attributeId, updatedDtos);
        return ResponseEntity.ok(response);
    }
}
