package com.saymyname.webapp.controller.admin;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.AttributeEnumOption;
import com.saymyname.service.AttributeEnumOptionService;
import com.saymyname.service.AttributeService;
import com.saymyname.webapp.dto.AttributeDto;
import com.saymyname.webapp.dto.AttributeEnumOptionDto;
import com.saymyname.webapp.dto.admin.AdminAttributeMutationDto;
import com.saymyname.webapp.dto.admin.AttributeOrderDto;
import com.saymyname.webapp.mapper.AttributeDtoMapper;
import com.saymyname.webapp.mapper.AttributeEnumOptionDtoMapper;
import com.saymyname.webapp.mapper.admin.AdminAttributeMutationDtoMapper;

@PreAuthorize("@orgSecurity.hasRole(null, 'ADMIN') or @orgSecurity.hasRole(null, 'OWNER')")
@RestController
@RequestMapping("/api/admin/attributes")
public class AdminAttributeController {

    private final AttributeService attributeService;
    private final AttributeEnumOptionService optionService;
    private final AttributeDtoMapper attributeMapper;
    private final AttributeEnumOptionDtoMapper optionMapper;
    private final AdminAttributeMutationDtoMapper mutationMapper;

    public AdminAttributeController(
            AttributeService attributeService,
            AttributeEnumOptionService optionService,
            AttributeDtoMapper attributeMapper,
            AttributeEnumOptionDtoMapper optionMapper,
            AdminAttributeMutationDtoMapper mutationMapper) {
        this.attributeService = attributeService;
        this.optionService = optionService;
        this.attributeMapper = attributeMapper;
        this.optionMapper = optionMapper;
        this.mutationMapper = mutationMapper;
    }

    @GetMapping
    public List<AttributeDto> list() {
        return toDtos(attributeService.findAll());
    }

    @GetMapping("/filterable")
    public List<AttributeDto> listFilterable() {
        return toDtos(attributeService.getFilterableAttributes());
    }

    @PostMapping
    public ResponseEntity<AttributeDto> create(@RequestBody AdminAttributeMutationDto request) {
        Attribute saved = attributeService.create(
                mutationMapper.toModel(null, request),
                request.enumOptions());
        return ResponseEntity
                .created(URI.create("/api/admin/attributes/" + saved.getId()))
                .body(toDto(saved));
    }

    @PutMapping("/{id}")
    public AttributeDto update(
            @PathVariable("id") Long id,
            @RequestBody AdminAttributeMutationDto request) {
        Attribute saved = attributeService.update(
                mutationMapper.toModel(id, request),
                request.enumOptions());
        return toDto(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        attributeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorder(@RequestBody List<AttributeOrderDto> request) {
        attributeService.reorder(request.stream()
                .map(item -> new AttributeService.OrderUpdate(item.id(), item.displayOrder()))
                .toList());
        return ResponseEntity.noContent().build();
    }

    private List<AttributeDto> toDtos(List<Attribute> attributes) {
        Map<Long, List<AttributeEnumOption>> optionsByAttribute = optionService
                .getActiveOptionsByAttributeIds(attributes.stream().map(Attribute::getId).toList());
        return attributes.stream()
                .map(attribute -> attributeMapper.toDto(
                        attribute,
                        null,
                        toOptionDtos(optionsByAttribute.get(attribute.getId()))))
                .toList();
    }

    private AttributeDto toDto(Attribute attribute) {
        return attributeMapper.toDto(
                attribute,
                null,
                toOptionDtos(optionService.getActiveOptionsByAttributeId(attribute.getId())));
    }

    private List<AttributeEnumOptionDto> toOptionDtos(List<AttributeEnumOption> options) {
        if (options == null) {
            return List.of();
        }
        return options.stream().map(optionMapper::toDto).collect(Collectors.toList());
    }
}
