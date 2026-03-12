package com.saymyname.webapp.controller;

import com.saymyname.core.model.people.Concept;
import com.saymyname.service.ConceptService;
import com.saymyname.webapp.dto.ConceptDto;
import com.saymyname.webapp.mapper.ConceptDtoMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/concepts")
public class ConceptRestController {

    private final ConceptService conceptService;
    private final ConceptDtoMapper conceptDtoMapper;

    public ConceptRestController(
            ConceptService conceptService,
            ConceptDtoMapper conceptDtoMapper) {
        this.conceptService = conceptService;
        this.conceptDtoMapper = conceptDtoMapper;
    }

    @GetMapping
    public ResponseEntity<List<ConceptDto>> getAllConcepts() {
        final List<Concept> concepts = conceptService.findAll();

        final List<ConceptDto> dtos = concepts.stream()
                .map(conceptDtoMapper::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }
}