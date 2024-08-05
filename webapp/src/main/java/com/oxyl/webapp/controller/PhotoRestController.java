package com.oxyl.webapp.controller;

import com.oxyl.core.model.Photo;
import com.oxyl.service.PhotoService;
import com.oxyl.webapp.dto.PhotoDto;
import com.oxyl.webapp.mapper.PhotoDtoMapper;
import org.apache.http.protocol.HTTP;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PhotoRestController {

    private final PhotoService photoService;
    private final PhotoDtoMapper photoDtoMapper;

    public PhotoRestController(PhotoService photoService, PhotoDtoMapper photoDtoMapper) {
        this.photoService = photoService;
        this.photoDtoMapper = photoDtoMapper;
    }

    @GetMapping("/api/quiz/photo/random")
    public ResponseEntity<PhotoDto> getRandomPhoto() {
        return new ResponseEntity<>(photoDtoMapper.toDto(photoService.getRandomPhoto()), HttpStatus.OK);
    }
}
