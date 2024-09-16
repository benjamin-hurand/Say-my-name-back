package com.oxyl.webapp.controller;

import com.oxyl.core.model.game.options.GameOptions;
import com.oxyl.core.model.people.Photo;
import com.oxyl.service.PersonAttributeService;
import com.oxyl.service.PhotoService;
import com.oxyl.webapp.dto.*;
import com.oxyl.webapp.mapper.GameOptionsDtoMapper;
import com.oxyl.webapp.mapper.PersonAttributeDtoMapper;
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
    private final GameOptionsDtoMapper gameOptionsDtoMapper;
    private final PersonAttributeService personAttributeService;
    private final PersonAttributeDtoMapper personAttributeDtoMapper;

    public PhotoRestController(
            PhotoService photoService,
            PhotoDtoMapper photoDtoMapper,
            GameOptionsDtoMapper gameOptionsDtoMapper,
            PersonAttributeService personAttributeService,
            PersonAttributeDtoMapper personAttributeDtoMapper) {
        this.photoService = photoService;
        this.photoDtoMapper = photoDtoMapper;
        this.gameOptionsDtoMapper = gameOptionsDtoMapper;
        this.personAttributeService = personAttributeService;
        this.personAttributeDtoMapper = personAttributeDtoMapper;
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
    @GetMapping("/{id}/person/attributes")
    public ResponseEntity<List<PersonAttributeDto>> getPersonAttributesByPhotoId(@PathVariable("id") Long id) {
        List<PersonAttributeDto> personAttributeDtoList = personAttributeService
                .getAttributesByPhotoId(id)
                .stream()
                .map(personAttributeDtoMapper::toDto)
                .toList();
        logger.info("Fetching attributes for photo ID {}: {}", id, personAttributeDtoList);
        return new ResponseEntity<>(personAttributeDtoList, HttpStatus.OK);
    }

    // Fetch a photo based on criteria: gameMode, filters, and sorting
    @PostMapping("/random/with-criteria")
    public ResponseEntity<PhotoDto> getRandomPhotoWithCriteria(
            @RequestBody(required = false) FetchPhotoCriteriaDto fetchPhotoCriteriaDto
    ) {
        logger.info("Fetching - Quiz options DTO : {} , and person ids historic: {}", fetchPhotoCriteriaDto.gameOptionsDto(), fetchPhotoCriteriaDto.personIdsHistoric());
        GameOptions options = gameOptionsDtoMapper.toModel(fetchPhotoCriteriaDto.gameOptionsDto());
        logger.info("Fetching - Quiz options : {} ", fetchPhotoCriteriaDto.gameOptionsDto());
        Photo photo = photoService.getNextPhotoWithCriteria(options, fetchPhotoCriteriaDto.personIdsHistoric());
        logger.info("Fetched photo - Quiz with options : {}", photo);
        PhotoDto photoDto = photoDtoMapper.toDto(photo);
        return new ResponseEntity<>(photoDto, HttpStatus.OK);
    }
}

