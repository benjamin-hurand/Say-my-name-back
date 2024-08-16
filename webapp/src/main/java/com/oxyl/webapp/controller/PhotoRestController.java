package com.oxyl.webapp.controller;

import com.oxyl.core.model.game.options.GameAttributeFilter;
import com.oxyl.core.model.game.options.GameOptions;
import com.oxyl.core.model.people.Person;
import com.oxyl.core.model.people.Photo;
import com.oxyl.core.model.game.options.GameMode;
import com.oxyl.service.PhotoService;
import com.oxyl.webapp.dto.PersonDto;
import com.oxyl.webapp.dto.PhotoDto;
import com.oxyl.webapp.dto.GameOptionsDto;
import com.oxyl.webapp.mapper.GameOptionsDtoMapper;
import com.oxyl.webapp.mapper.PersonDtoMapper;
import com.oxyl.webapp.mapper.PhotoDtoMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/photos")  // Common base path for photo-related operations
public class PhotoRestController {
    private static final Logger logger = LogManager.getLogger(PhotoRestController.class);
    private final PhotoService photoService;
    private final PhotoDtoMapper photoDtoMapper;
    private final PersonDtoMapper personDtoMapper;
    private final GameOptionsDtoMapper gameOptionsDtoMapper;

    public PhotoRestController(
            PhotoService photoService,
            PhotoDtoMapper photoDtoMapper,
            PersonDtoMapper personDtoMapper,
            GameOptionsDtoMapper gameOptionsDtoMapper) {
        this.photoService = photoService;
        this.photoDtoMapper = photoDtoMapper;
        this.personDtoMapper = personDtoMapper;
        this.gameOptionsDtoMapper = gameOptionsDtoMapper;
    }

    // Fetch a random photo
    @GetMapping("/random")
    public ResponseEntity<PhotoDto> getRandomPhoto() {
        Photo photo = photoService.getRandomPhoto();
        PhotoDto photoDto = photoDtoMapper.toDto(photo);
        logger.info("Fetching random photo: {}", photo);
        return new ResponseEntity<>(photoDto, HttpStatus.OK);
    }

    // Fetch the person associated with a photo by photo ID
    @GetMapping("/{id}/person")
    public ResponseEntity<PersonDto> getPersonByPhotoId(@PathVariable Long id) {
        Person person = photoService.findPersonByPhotoId(id);
        PersonDto personDto = personDtoMapper.toDto(person);
        logger.info("Fetching person for photo ID {}: {}", id, person);
        return new ResponseEntity<>(personDto, HttpStatus.OK);
    }

    // Fetch a photo based on criteria: gameMode, filters, and sorting
    @PostMapping("/random/with-criteria")
    public ResponseEntity<PhotoDto> getRandomPhotoWithCriteria(
            @RequestBody(required = false) GameOptionsDto gameOptionsDto) {
        logger.info("Fetching - Quiz options DTO : {} ", gameOptionsDto);
        GameOptions options = gameOptionsDtoMapper.toModel(gameOptionsDto);
        logger.info("Fetching - Quiz options : {} ", gameOptionsDto);
        Photo photo = photoService.getRandomPhotoWithCriteria(options);
        PhotoDto photoDto = photoDtoMapper.toDto(photo);
        return new ResponseEntity<>(photoDto, HttpStatus.OK);
    }
}

