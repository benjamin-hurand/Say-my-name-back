package com.saymyname.webapp.mapper.course;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.Population;
import com.saymyname.webapp.dto.course.PopulationDto;
import com.saymyname.webapp.mapper.AttributeDtoMapper;
import com.saymyname.webapp.mapper.UserDtoMapper;

@Component
public class PopulationDtoMapper {

    private final AttributeDtoMapper attributeDtoMapper;
    private final UserDtoMapper userDtoMapper;

    public PopulationDtoMapper(AttributeDtoMapper attributeDtoMapper, UserDtoMapper userDtoMapper) {
        this.attributeDtoMapper = attributeDtoMapper;
        this.userDtoMapper = userDtoMapper;
    }

    public PopulationDto toDto(Population population) {
        return new PopulationDto(
                Long.valueOf(population.getId()),
                population.getTitle(),
                population.getDescription(),
                attributeDtoMapper.toDto(population.getAttributeFilter()),
                population.getMinValue(),
                population.getMaxValue(),
                userDtoMapper.toDto(population.getCreatedBy()),
                population.getCount());
    }

    public Population toModel(PopulationDto dto) {
        return new Population.Builder()
                .withId(dto.id())
                .withTitle(dto.title())
                .withDescription(dto.description())
                .withAttributeFilter(attributeDtoMapper.toModel(dto.attributeFilter()))
                .withMinValue(dto.minValue())
                .withMaxValue(dto.maxValue())
                .withCreatedBy(userDtoMapper.toModel(dto.createdBy()))
                .withCount(dto.personCount())
                .build();
    }

    public Population toModel(Long populationId) {
        return new Population.Builder()
                .withId(populationId)
                .build();
    }
}
