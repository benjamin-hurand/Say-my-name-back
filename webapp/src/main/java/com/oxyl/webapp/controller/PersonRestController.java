package com.oxyl.webapp.controller;

import com.oxyl.core.model.Person;
import com.oxyl.service.PersonService;
import com.oxyl.webapp.dto.PersonDto;
import com.oxyl.webapp.mapper.PersonDtoMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger logger = LogManager.getLogger(PersonRestController.class);


    public PersonRestController(PersonService personService, PersonDtoMapper personDtoMapper) {
        this.personService = personService;
        this.personDtoMapper = personDtoMapper;
    }

    @GetMapping
    public ResponseEntity<List<Person>> findAll() {
        List<Person> persons = personService.findAll();
        return new ResponseEntity<>(persons, HttpStatus.OK);
    }

    @GetMapping("/withoutaccount")
    public ResponseEntity<List<PersonDto>> findAllWithoutAcccount() {
        List<PersonDto> persons = personService.findAllWithoutUser().stream().map(personDtoMapper::toDto).toList();
        return new ResponseEntity<>(persons, HttpStatus.OK);
    }

    @GetMapping("/by-year/{year}")
    public ResponseEntity<List<Person>> findAllByPromotionYear(@PathVariable(name="year") Integer year) {
        List<Person> persons = personService.findByPromotionYear(year);
        return new ResponseEntity<>(persons, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> getById(@PathVariable(name="id") Long id) {
        Person person = personService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No person found with ID: " + id));
        return ResponseEntity.ok(person);
    }




//    @GetMapping("/persons")
//    public ResponseEntity<List<Person>> findAllPersons() {
//        List<Person> persons = personService.findAllPersons();
//        return new ResponseEntity<>(persons, HttpStatus.OK);
//    }
}
