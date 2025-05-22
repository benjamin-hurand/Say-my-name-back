package com.saymyname.webapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.people.Person;
import com.saymyname.service.PersonAttributeService;
import com.saymyname.service.PersonService;
import com.saymyname.webapp.dto.PersonAttributeDto;
import com.saymyname.webapp.mapper.PersonAttributeDtoMapper;

@RestController
@RequestMapping("/api/persons")
public class PersonRestController {

    private final PersonService personService;
    private final PersonAttributeService personAttributeService;
    private final PersonAttributeDtoMapper personAttributeDtoMapper;

    public PersonRestController(
            PersonService personService,
            PersonAttributeService personAttributeService,
            PersonAttributeDtoMapper personAttributeDtoMapper) {
        this.personService = personService;
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
    public ResponseEntity<Person> getById(@PathVariable(name = "id") Long id) {
        Person person = personService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No person found with ID: " + id));
        return ResponseEntity.ok(person);
    }

    @GetMapping("/{id}/attributes")
    public ResponseEntity<List<PersonAttributeDto>> getAttributesById(@PathVariable(name = "id") Long id) {
        List<PersonAttributeDto> personAttributeDtoList = personAttributeService
                .getAttributesByPersonId(id)
                .stream()
                .map(personAttributeDtoMapper::toDto)
                .toList();
        // personAttributeDtoList);
        return new ResponseEntity<>(personAttributeDtoList, HttpStatus.OK);
    }

}
