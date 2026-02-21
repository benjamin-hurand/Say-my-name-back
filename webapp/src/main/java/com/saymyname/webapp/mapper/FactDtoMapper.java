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

    public FactDto toDto(Fact fact) {
        if (fact == null) {
            return null;
        }
        return new FactDto(
                fact.getId(),
                fact.getAttribute().getId(),
                fact.getValue(),
                fact.getValidFrom(),
                fact.getValidTo(),
                Boolean.TRUE.equals(fact.isDeleted()));
    }

    public ReducedFactDto toReducedDto(Fact fact) {
        if (fact == null) {
            return null;
        }
        return new ReducedFactDto(
                fact.getId(),
                attributeDtoMapper.toDto(fact.getAttribute()),
                fact.getValue());
    }

    public FactLiteDto toLiteDto(Fact fact) {
        if (fact == null) {
            return null;
        }
        return new FactLiteDto(
                fact.getId(),
                attributeDtoMapper.toDto(fact.getAttribute()),
                fact.getValue(),
                fact.getPerson().getId());
    }

    public FactMinimalDto toMinimalDto(Fact fact) {
        if (fact == null) {
            return null;
        }
        return new FactMinimalDto(
                fact.getId(),
                fact.getValue());
    }

    public Fact patchDtoToModel(FactPatch dto) {
        return new Fact.Builder()
                .withId(dto.id())
                .withValue(dto.value())
                .build();
    }
}
