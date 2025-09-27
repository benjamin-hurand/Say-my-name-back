// src/main/java/com/saymyname/webapp/controller/AttributeRestController.java
package com.saymyname.webapp.controller;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.AttributeType;
// ⚠️ Assure-toi d'importer la bonne classe ObservedMinMax (stats vs people)
// import com.saymyname.core.model.stats.ObservedMinMax;
import com.saymyname.core.model.people.ObservedMinMax;

import com.saymyname.service.AttributeEnumOptionService;
import com.saymyname.service.AttributeService;
import com.saymyname.service.PersonAttributeService;
import com.saymyname.webapp.dto.AttributeDto;
import com.saymyname.webapp.dto.AttributeEnumOptionDto;
import com.saymyname.webapp.dto.AttributeStatsDto;
import com.saymyname.webapp.mapper.AttributeDtoMapper;
import com.saymyname.webapp.mapper.AttributeEnumOptionDtoMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attributes")
public class AttributeRestController {

    private final AttributeService attributeService;
    private final AttributeDtoMapper attributeDtoMapper;
    private final PersonAttributeService personAttributeService;
    private final AttributeEnumOptionService attributeEnumOptionService;
    private final AttributeEnumOptionDtoMapper attributeEnumOptionDtoMapper;

    public AttributeRestController(AttributeService attributeService,
            AttributeDtoMapper attributeDtoMapper,
            PersonAttributeService personAttributeService,
            AttributeEnumOptionService attributeEnumOptionService,
            AttributeEnumOptionDtoMapper attributeEnumOptionDtoMapper) {
        this.attributeService = attributeService;
        this.attributeDtoMapper = attributeDtoMapper;
        this.personAttributeService = personAttributeService;
        this.attributeEnumOptionService = attributeEnumOptionService;
        this.attributeEnumOptionDtoMapper = attributeEnumOptionDtoMapper;
    }

    @GetMapping
    public ResponseEntity<List<AttributeDto>> getAllAttributes(
            @RequestParam(name = "expand", required = false) String expand) {
        final boolean wantStats = hasExpand(expand, "stats");
        final boolean wantOptions = hasExpand(expand, "options");

        final List<Attribute> attrs = attributeService.findAll();
        final Map<Long, ObservedMinMax> rangeById = wantStats
                ? personAttributeService.computeObservedMinMaxByAttributes(attrs)
                : Collections.emptyMap();
        final Map<Long, List<AttributeEnumOptionDto>> optionsByAttrId = wantOptions ? fetchEnumOptions(attrs)
                : Collections.emptyMap();

        final List<AttributeDto> dtos = attrs.stream()
                .map(a -> attributeDtoMapper.toDto(
                        a,
                        toStats(rangeById.get(a.getId())),
                        optionsByAttrId.get(a.getId())))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/filters")
    public ResponseEntity<List<AttributeDto>> getFilterableAttributes(
            @RequestParam(name = "expand", required = false) String expand) {
        final boolean wantStats = hasExpand(expand, "stats");
        final boolean wantOptions = hasExpand(expand, "options");

        final List<Attribute> attrs = attributeService.getFilterableAttributes();
        final Map<Long, ObservedMinMax> rangeById = wantStats
                ? personAttributeService.computeObservedMinMaxByAttributes(attrs)
                : Collections.emptyMap();
        final Map<Long, List<AttributeEnumOptionDto>> optionsByAttrId = wantOptions ? fetchEnumOptions(attrs)
                : Collections.emptyMap();

        final List<AttributeDto> dtos = attrs.stream()
                .map(a -> attributeDtoMapper.toDto(
                        a,
                        toStats(rangeById.get(a.getId())),
                        optionsByAttrId.get(a.getId())))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/sorts")
    public ResponseEntity<List<AttributeDto>> getAllSorts(
            @RequestParam(name = "expand", required = false) String expand) {
        final boolean wantStats = hasExpand(expand, "stats");
        final boolean wantOptions = hasExpand(expand, "options");

        final List<Attribute> attrs = attributeService.findAllSorts();
        final Map<Long, ObservedMinMax> rangeById = wantStats
                ? personAttributeService.computeObservedMinMaxByAttributes(attrs)
                : Collections.emptyMap();
        final Map<Long, List<AttributeEnumOptionDto>> optionsByAttrId = wantOptions ? fetchEnumOptions(attrs)
                : Collections.emptyMap();

        final List<AttributeDto> dtos = attrs.stream()
                .map(a -> attributeDtoMapper.toDto(
                        a,
                        toStats(rangeById.get(a.getId())),
                        optionsByAttrId.get(a.getId())))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    // --------- helpers ---------

    private boolean hasExpand(String expand, String key) {
        if (expand == null || expand.isBlank())
            return false;
        return Arrays.stream(expand.split(","))
                .map(String::trim)
                .anyMatch(s -> s.equalsIgnoreCase(key));
    }

    private Map<Long, List<AttributeEnumOptionDto>> fetchEnumOptions(List<Attribute> attrs) {
        final Set<Long> enumIds = attrs.stream()
                .filter(a -> a.getType() == AttributeType.ENUM)
                .map(Attribute::getId)
                .collect(Collectors.toSet());
        if (enumIds.isEmpty())
            return Collections.emptyMap();

        return attributeEnumOptionService.getActiveOptionsByAttributeIds(enumIds)
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .map(attributeEnumOptionDtoMapper::toDto)
                                .toList()));
    }

    private AttributeStatsDto toStats(ObservedMinMax mm) {
        return (mm == null) ? null : new AttributeStatsDto(mm.min(), mm.max());
    }
}
