package com.saymyname.webapp.controller.profile;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.common.User;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.service.ChangeRequestService;
import com.saymyname.service.PersonService;
import com.saymyname.service.UserService;
import com.saymyname.webapp.dto.PersonAttributeDto;
import com.saymyname.webapp.dto.PersonDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestSummaryDto;
import com.saymyname.webapp.dto.profile.AttributeValuesResponseDto;
import com.saymyname.webapp.dto.profile.BulkPersonAttributeRequest;
import com.saymyname.webapp.dto.profile.ProfileResponseDto;
import com.saymyname.webapp.mapper.BulkPersonAttributeDtoMapper;
import com.saymyname.webapp.mapper.ChangeRequestDtoMapper;
import com.saymyname.webapp.mapper.PersonAttributeDtoMapper;
import com.saymyname.webapp.mapper.PersonDtoMapper;

@RestController
@RequestMapping("/api/profile")
public class ProfileRestController {

    private final PersonService personService;
    private final ChangeRequestService changeRequestService;
    private final ChangeRequestDtoMapper changeRequestDtoMapper;
    private final PersonDtoMapper personDtoMapper;
    private final BulkPersonAttributeDtoMapper bulkPersonAttributeDtoMapper;
    private final PersonAttributeDtoMapper personAttributeDtoMapper;
    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(ProfileRestController.class);

    public ProfileRestController(PersonService personService,
            ChangeRequestService changeRequestService,
            ChangeRequestDtoMapper changeRequestDtoMapper,
            PersonDtoMapper personDtoMapper,
            BulkPersonAttributeDtoMapper bulkPersonAttributeDtoMapper,
            PersonAttributeDtoMapper personAttributeDtoMapper,
            UserService userService) {
        this.personService = personService;
        this.changeRequestService = changeRequestService;
        this.changeRequestDtoMapper = changeRequestDtoMapper;
        this.personDtoMapper = personDtoMapper;
        this.bulkPersonAttributeDtoMapper = bulkPersonAttributeDtoMapper;
        this.personAttributeDtoMapper = personAttributeDtoMapper;
        this.userService = userService;
    }

    /**
     * GET /api/profile
     * Récupère le profil (user + person + changeRequests).
     * Si aucune Person n'est associée, renvoie person=null et changeRequests=[].
     */
    @GetMapping
    public ResponseEntity<ProfileResponseDto> getProfile(Principal principal) {
        // 0) User courant
        User user = userService.getCurrentUserOrThrow(principal);

        // 1) Person + graph (attributs, photos)
        Optional<Person> optPerson = personService.getPersonByUserWithAllAttributes(user);
        PersonDto personDto = optPerson.map(personDtoMapper::toDto).orElse(null);

        // 2) ChangeRequests (choix: “open” ou “all”)
        List<ChangeRequestSummaryDto> crDtos = optPerson.isPresent()
                ? changeRequestService.findOpenForUser(user.getId()).stream()
                        .map(changeRequestDtoMapper::toSummaryDto)
                        .toList()
                : List.of();

        // 3) Réponse
        ProfileResponseDto response = new ProfileResponseDto(personDto, crDtos);
        return ResponseEntity.ok(response);
    }

    // ---------- BULK multi-valeurs pour un attribut ----------
    /**
     * POST /api/profile/attributes/{attributeId}/bulk
     * Applique en une fois create/update/delete pour l’attribut {attributeId}.
     * On s’appuie sur l’utilisateur courant (Principal) pour identifier la Person.
     * Retourne l'état canonique de l'attribut (liste de PersonAttribute) après
     * normalisation.
     */
    @PostMapping("/attributes/{attributeId}/bulk")
    public ResponseEntity<AttributeValuesResponseDto> applyAttributeChanges(
            @PathVariable("attributeId") Long attributeId,
            @RequestBody BulkPersonAttributeRequest body,
            Principal principal) {

        logger.info("Applying bulk attribute changes for attributeId: {}", attributeId);

        // 0) User courant (→ person)
        User user = userService.getCurrentUserOrThrow(principal);

        // 1) Map DTO -> modèles (delta)
        var toCreate = bulkPersonAttributeDtoMapper.toCreateModels(body.create());
        var toUpdate = bulkPersonAttributeDtoMapper.toUpdateModels(body.update());
        var toDelete = bulkPersonAttributeDtoMapper.toDeleteModels(body.delete());

        // 2) Orchestration via PersonService (référence “profil” = person de l'user)
        List<PersonAttribute> updatedAttributes = personService.applyAttributeChangesForUser(
                user,
                attributeId,
                toCreate,
                toUpdate,
                toDelete);

        // 3) Model -> DTO
        List<PersonAttributeDto> updatedDtos = updatedAttributes.stream()
                .map(personAttributeDtoMapper::toDto)
                .toList();

        var response = new AttributeValuesResponseDto(attributeId, updatedDtos);
        return ResponseEntity.ok(response);
    }
}
