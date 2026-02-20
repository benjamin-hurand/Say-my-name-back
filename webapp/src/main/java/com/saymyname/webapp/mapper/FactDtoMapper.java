package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.Fact;
import com.saymyname.webapp.dto.FactDto;
import com.saymyname.webapp.dto.FactLiteDto;
import com.saymyname.webapp.dto.ReducedFactDto;
import com.saymyname.webapp.dto.person.FactMinimalDto;
import com.saymyname.webapp.dto.profile.FactPatch;

@Component
public class FactDtoMapper {

    private final AttributeDtoMapper attributeDtoMapper;

    public FactDtoMapper(AttributeDtoMapper attributeDtoMapper) {
        this.attributeDtoMapper = attributeDtoMapper;
    }

    public FactDto toDto(Fact personAttribute) {
        if (personAttribute == null) {
            return null;
        }
        return new FactDto(
                personAttribute.getId(),
                personAttribute.getAttribute().getId(),
                personAttribute.getValue(),
                personAttribute.getValidFrom(),
                personAttribute.getValidTo(),
                Boolean.TRUE.equals(personAttribute.isDeleted()));
    }

    public ReducedFactDto toReducedDto(Fact personAttribute) {
        if (personAttribute == null) {
            return null;
        }
        return new ReducedFactDto(
                personAttribute.getId(),
                attributeDtoMapper.toDto(personAttribute.getAttribute()),
                personAttribute.getValue());
    }

    public FactLiteDto toLiteDto(Fact personAttribute) {
        if (personAttribute == null) {
            return null;
        }
        return new FactLiteDto(
                personAttribute.getId(),
                attributeDtoMapper.toDto(personAttribute.getAttribute()),
                personAttribute.getValue(),
                personAttribute.getPerson().getId());
    }

    public FactMinimalDto toMinimalDto(Fact personAttribute) {
        if (personAttribute == null) {
            return null;
        }
        return new FactMinimalDto(
                personAttribute.getId(),
                personAttribute.getValue());
    }

    public Fact patchDtoToModel(FactPatch dto) {
        return Fact.builder()
                .id(dto.id())
                .value(dto.value())
                .build();
    }
}
