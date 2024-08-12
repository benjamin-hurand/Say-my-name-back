package com.oxyl.webapp.controller;

import com.oxyl.service.AttributeService;
import com.oxyl.webapp.dto.AttributeDto;
import com.oxyl.webapp.mapper.AttributeDtoMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AttributeRestController {

    private final AttributeService attributeService;
    private final AttributeDtoMapper attributeDtoMapper;

    public AttributeRestController(AttributeService attributeService, AttributeDtoMapper attributeDtoMapper) {
        this.attributeService = attributeService;
        this.attributeDtoMapper = attributeDtoMapper;
    }

    @GetMapping("/api/quiz/attributes")
    public ResponseEntity<List<AttributeDto>> getAllAttributes() {
        List<AttributeDto> attributes = attributeService.findAllAttributes().stream().map(attributeDtoMapper::toDto).toList();
        return ResponseEntity.ok(attributes);
    }

    @GetMapping("/api/quiz/filters")
    public ResponseEntity<List<AttributeDto>> getAllFilters() {
        List<AttributeDto> attributes = attributeService.findAllFilters().stream().map(attributeDtoMapper::toDto).toList();
        return ResponseEntity.ok(attributes);
    }
}
