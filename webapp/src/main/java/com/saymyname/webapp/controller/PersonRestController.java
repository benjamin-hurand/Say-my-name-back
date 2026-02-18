// src/main/java/com/saymyname/webapp/controller/PersonRestController.java
package com.saymyname.webapp.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.persondirectory.PersonCard;
import com.saymyname.core.model.persondirectory.PersonSearchCriteria;
import com.saymyname.service.FactService;
import com.saymyname.service.UserService;
import com.saymyname.service.person.PersonService;
import com.saymyname.webapp.dto.FactLiteDto;
import com.saymyname.webapp.dto.person.PersonCardDto;
import com.saymyname.webapp.dto.person.PersonSearchRequestDto;
import com.saymyname.webapp.mapper.FactDtoMapper;
import com.saymyname.webapp.mapper.person.PersonDirectoryDtoMapper;

@RestController
@RequestMapping("/api/persons")
public class PersonRestController {

    private final PersonService personService;
    private final FactService personAttributeService;
    private final FactDtoMapper personAttributeDtoMapper;
    private final PersonDirectoryDtoMapper personDirectoryDtoMapper;
    private final UserService userService;

    public PersonRestController(
            PersonService personService,
            FactService personAttributeService,
            FactDtoMapper personAttributeDtoMapper,
            PersonDirectoryDtoMapper personDirectoryDtoMapper,
            UserService userService) {
        this.personService = personService;
        this.personAttributeService = personAttributeService;
        this.personAttributeDtoMapper = personAttributeDtoMapper;
        this.personDirectoryDtoMapper = personDirectoryDtoMapper;
        this.userService = userService;
    }

    /** EXISTANT — on garde tel quel */
    @GetMapping("/{id}/attributes")
    public ResponseEntity<List<FactLiteDto>> getAttributesById(@PathVariable(name = "id") Long id) {
        List<FactLiteDto> personAttributeDtoList = personAttributeService
                .getFactsByPersonId(id)
                .stream()
                .map(personAttributeDtoMapper::toLiteDto)
                .toList();
        return new ResponseEntity<>(personAttributeDtoList, HttpStatus.OK);
    }

    /** NOUVEAU — recherche trombinoscope filtrée/triée/paginée */
    @PostMapping("/search")
    public ResponseEntity<Page<PersonCardDto>> searchPersons(
            @RequestBody PersonSearchRequestDto body,
            Pageable pageable,
            Principal principal) {

        Long currentUserId = userService.getCurrentUserOrThrow(principal).getId();
        PersonSearchCriteria criteria = personDirectoryDtoMapper.toModel(body);

        Page<PersonCard> page = personService.searchPersons(criteria, pageable, currentUserId);
        Page<PersonCardDto> dtoPage = page.map(personDirectoryDtoMapper::toDto);

        return ResponseEntity.ok(dtoPage);
    }
}
