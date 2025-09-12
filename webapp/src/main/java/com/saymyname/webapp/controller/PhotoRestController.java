package com.saymyname.webapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.people.Photo;
import com.saymyname.security.CustomUserDetails;
import com.saymyname.service.profile.PhotoService;
import com.saymyname.webapp.dto.PhotoDto;
import com.saymyname.webapp.mapper.PhotoDtoMapper;

@RestController
@RequestMapping("/api")
public class PhotoRestController {

    private final PhotoService photoService;
    private final PhotoDtoMapper photoDtoMapper;

    public PhotoRestController(PhotoService photoService, PhotoDtoMapper photoDtoMapper) {
        this.photoService = photoService;
        this.photoDtoMapper = photoDtoMapper;
    }

    /**
     * POST /persons/{personId}/photos
     * Soumet une photo pour approbation (status = PENDING).
     */
    @PostMapping(path = "/persons/{personId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoDto> submitPhotoForApproval(
            @PathVariable("personId") Long personId,
            @RequestPart("photo") MultipartFile photo,
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (photo == null || photo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Fichier manquant");
        }
        validateContentType(photo);

        // 1) Délégation complète au service (stockage + BDD)
        Photo created = photoService.submitPhotoForApproval(personId, photo, principal);

        // 2) Répondre avec le DTO
        PhotoDto body = photoDtoMapper.toDto(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    // --- Helpers ---
    private static void validateContentType(MultipartFile file) {
        String ct = file.getContentType();
        if (ct == null) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Type de fichier inconnu");
        }
        switch (ct) {
            case "image/jpeg":
            case "image/png":
            case "image/webp":
                return;
            default:
                throw new ResponseStatusException(
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        "Formats autorisés: JPG, PNG, WEBP");
        }
    }
}
