package com.saymyname.webapp.controller.course;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saymyname.service.course.PopulationService;
import com.saymyname.webapp.dto.course.PopulationDto;
import com.saymyname.webapp.mapper.course.PopulationDtoMapper;

@RestController
@RequestMapping("/api/populations")
public class PopulationRestController {

    private final PopulationService populationService;
    private final PopulationDtoMapper populationDtoMapper;

    public PopulationRestController(PopulationService populationService, PopulationDtoMapper populationDtoMapper) {
        this.populationService = populationService;
        this.populationDtoMapper = populationDtoMapper;
    }

    @GetMapping
    public ResponseEntity<List<PopulationDto>> getAllPopulations() {
        List<PopulationDto> populations = populationService.getAllPopulations()
                .stream()
                .map(populationDtoMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(populations);
    }
}
