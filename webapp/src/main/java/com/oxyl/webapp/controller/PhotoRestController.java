package com.oxyl.webapp.controller;

import com.oxyl.core.model.Person;
import com.oxyl.core.model.Photo;
import com.oxyl.service.PhotoService;
import com.oxyl.webapp.dto.PersonDto;
import com.oxyl.webapp.dto.PhotoDto;
import com.oxyl.webapp.mapper.PersonDtoMapper;
import com.oxyl.webapp.mapper.PhotoDtoMapper;
import org.apache.http.protocol.HTTP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PhotoRestController {
    private static final Logger logger = LogManager.getLogger(PhotoRestController.class);
    private final PhotoService photoService;
    private final PhotoDtoMapper photoDtoMapper;
    private final PersonDtoMapper personDtoMapper;

    public PhotoRestController(PhotoService photoService, PhotoDtoMapper photoDtoMapper, PersonDtoMapper personDtoMapper) {
        this.photoService = photoService;
        this.photoDtoMapper = photoDtoMapper;
        this.personDtoMapper = personDtoMapper;
    }

    @GetMapping("/api/quiz/photo/random")
    public ResponseEntity<PhotoDto> getRandomPhoto() {
        logger.info("random photo in rest controller : photo:{} /// dto:{} ", photoService.getRandomPhoto(), photoDtoMapper.toDto(photoService.getRandomPhoto()));
        return new ResponseEntity<>(photoDtoMapper.toDto(photoService.getRandomPhoto()), HttpStatus.OK);
    }

    @GetMapping("api/quiz/person/by-photo-id/{id}")
    public ResponseEntity<PersonDto> getPersonOfPhoto(@PathVariable(name="id") Long id) {
        Person person = photoService.findPersonByPhotoId(id);
        PersonDto personDto = personDtoMapper.toDto(person);
        logger.info("person photo in rest controller : person:{}", person);
        return new ResponseEntity<>(personDto, HttpStatus.OK);
    }
}
