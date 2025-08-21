package com.saymyname.webapp.controller.profile;

import java.security.Principal;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.service.profile.ProfileService;
import com.saymyname.webapp.dto.PersonDto;
import com.saymyname.webapp.dto.profile.CreatePersonAttributeRequest;
import com.saymyname.webapp.dto.profile.ProfileResponseDto;
import com.saymyname.webapp.dto.profile.UpdatePersonAttributesRequest;
import com.saymyname.webapp.mapper.PersonAttributeDtoMapper;
import com.saymyname.webapp.mapper.PersonDtoMapper;

@RestController
@RequestMapping("/api/profile")
public class ProfileRestController {

    private final ProfileService profileService;
    private final PersonDtoMapper personDtoMapper;
    private final PersonAttributeDtoMapper personAttributeDtoMapper;

    public ProfileRestController(ProfileService profileService, PersonDtoMapper personDtoMapper,
            PersonAttributeDtoMapper personAttributeDtoMapper) {
        this.profileService = profileService;
        this.personDtoMapper = personDtoMapper;
        this.personAttributeDtoMapper = personAttributeDtoMapper;
    }

    /**
     * GET /api/profile
     * Récupère le profil (user + person) de l'utilisateur connecté.
     * Si aucune Person n'est associée, renvoie person=null dans le DTO.
     */
    @GetMapping
    public ResponseEntity<ProfileResponseDto> getProfile(Principal principal) {
        // Récupère l'Optional<Person> depuis le service
        Optional<Person> optPerson = profileService.getProfile(principal.getName());

        // Mappe en DTO, ou null si absent
        PersonDto personDto = optPerson
                .map(personDtoMapper::toDto)
                .orElse(null);

        // Construit et renvoie le DTO de réponse
        ProfileResponseDto response = new ProfileResponseDto(personDto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/attributes")
    public ResponseEntity<ProfileResponseDto> patchAttributes(
            @RequestBody UpdatePersonAttributesRequest body,
            Principal principal) {
        // body.attributes: [{ id, value }]
        var models = body.attributes().stream()
                .map(personAttributeDtoMapper::patchDtoToModel)
                .toList();

        profileService.updatePersonAttributes(principal.getName(), models);

        // renvoyer le profil à jour puisque le front l'attend
        var personOpt = profileService.getProfile(principal.getName());
        var personDto = personOpt.map(personDtoMapper::toDto).orElse(null);
        return ResponseEntity.ok(new ProfileResponseDto(personDto));
    }

    // ---------- POST ATTRIBUTE (création d'une valeur) ----------
    // Correspond à createPersonAttribute(attributeId, value) côté front

    @PostMapping("/attributes")
    public ResponseEntity<?> createPersonAttribute(
            @RequestBody CreatePersonAttributeRequest body,
            Principal principal) {
        PersonAttribute created = profileService.createPersonAttribute(
                principal.getName(),
                body.attributeId(),
                body.value());
        // Le front attend un PersonAttribute "plat" (id, attribute, value, personId)
        var dto = personAttributeDtoMapper.toDto(created);
        return ResponseEntity.ok(dto);
    }

    // ---------- DELETE ATTRIBUTE (suppression d'une valeur) ----------
    @DeleteMapping("/attributes/{id}")
    public ResponseEntity<Void> deletePersonAttribute(
            @PathVariable("id") Long personAttributeId,
            Principal principal) {
        profileService.deletePersonAttribute(principal.getName(), personAttributeId);
        return ResponseEntity.noContent().build();
    }

    // ---------- PATCH ROOT (mise à jour username / email) ----------
    // Correspond à updateAccount(username, email) côté front
    public record UpdateAccountRequest(String username, String email) {
    }
}
