package com.saymyname.webapp.controller;

import com.saymyname.core.model.people.Person;
import com.saymyname.service.PersonAttributeService;
import com.saymyname.service.PersonService;
import com.saymyname.webapp.dto.PersonAttributeDto;
import com.saymyname.webapp.dto.PersonDto;
import com.saymyname.webapp.mapper.PersonAttributeDtoMapper;
import com.saymyname.webapp.mapper.PersonDtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/persons")
public class PersonRestController {

    private final PersonService personService;
    private final PersonDtoMapper personDtoMapper;
    private final PersonAttributeService personAttributeService;
    private final PersonAttributeDtoMapper personAttributeDtoMapper;
    private static final Logger logger = LoggerFactory.getLogger(PersonRestController.class);


    public PersonRestController(
            PersonService personService,
            PersonDtoMapper personDtoMapper,
            PersonAttributeService personAttributeService,
            PersonAttributeDtoMapper personAttributeDtoMapper) {
        this.personService = personService;
        this.personDtoMapper = personDtoMapper;
        this.personAttributeService = personAttributeService;
        this.personAttributeDtoMapper = personAttributeDtoMapper;
    }

    @GetMapping
    public ResponseEntity<List<Person>> findAll() {
        List<Person> persons = personService.findAll();
        // logger.info("find all persons ? => " + persons);
        return new ResponseEntity<>(persons, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> getById(@PathVariable(name="id") Long id) {
        Person person = personService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No person found with ID: " + id));
        return ResponseEntity.ok(person);
    }

    @GetMapping("/{id}/attributes")
    public ResponseEntity<List<PersonAttributeDto>> getAttributesById(@PathVariable(name="id") Long id) {
        List<PersonAttributeDto> personAttributeDtoList = personAttributeService
                .getAttributesByPhotoId(id)
                .stream()
                .map(personAttributeDtoMapper::toDto)
                .toList();
        // logger.info("Fetching attributes for photo ID {}: {}", id, personAttributeDtoList);
        return new ResponseEntity<>(personAttributeDtoList, HttpStatus.OK);
    }

}
