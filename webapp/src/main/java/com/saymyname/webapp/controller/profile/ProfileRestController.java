package com.saymyname.webapp.controller.profile;

import java.security.Principal;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saymyname.core.model.people.Person;
import com.saymyname.service.profile.ProfileService;
import com.saymyname.webapp.dto.PersonDto;
import com.saymyname.webapp.dto.profile.ProfileResponseDto;
import com.saymyname.webapp.mapper.PersonDtoMapper;

@RestController
@RequestMapping("/api/profile")
public class ProfileRestController {

    private final ProfileService profileService;
    private final PersonDtoMapper personDtoMapper;

    public ProfileRestController(ProfileService profileService, PersonDtoMapper personDtoMapper) {
        this.profileService = profileService;
        this.personDtoMapper = personDtoMapper;
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
}
