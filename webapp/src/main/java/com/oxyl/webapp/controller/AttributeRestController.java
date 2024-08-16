package com.oxyl.webapp.controller;

import com.oxyl.service.AttributeService;
import com.oxyl.webapp.dto.AttributeDto;
import com.oxyl.webapp.mapper.AttributeDtoMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/attributes")
public class AttributeRestController {

    private final AttributeService attributeService;
    private final AttributeDtoMapper attributeDtoMapper;

    public AttributeRestController(AttributeService attributeService, AttributeDtoMapper attributeDtoMapper) {
        this.attributeService = attributeService;
        this.attributeDtoMapper = attributeDtoMapper;
    }

    @GetMapping
    public ResponseEntity<List<AttributeDto>> getAllAttributes() {
        List<AttributeDto> attributes = attributeService.findAllAttributes().stream().map(attributeDtoMapper::toDto).toList();
        return ResponseEntity.ok(attributes);
    }

    @GetMapping("/filters")
    public ResponseEntity<List<AttributeDto>> getAllFilters() {
        List<AttributeDto> attributes = attributeService.findAllFilters().stream().map(attributeDtoMapper::toDto).toList();
        return ResponseEntity.ok(attributes);
    }

    @GetMapping("/sorts")
    public ResponseEntity<List<AttributeDto>> getAllSorts() {
        List<AttributeDto> attributes = attributeService.findAllSorts().stream().map(attributeDtoMapper::toDto).toList();
        return ResponseEntity.ok(attributes);
    }
}
