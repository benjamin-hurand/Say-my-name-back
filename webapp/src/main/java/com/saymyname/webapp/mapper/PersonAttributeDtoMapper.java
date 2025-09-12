package com.saymyname.webapp.mapper;

import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.webapp.dto.PersonAttributeDto;
import com.saymyname.webapp.dto.PersonAttributeLiteDto;
import com.saymyname.webapp.dto.ReducedPersonAttributeDto;
import com.saymyname.webapp.dto.profile.PersonAttributePatch;

import org.springframework.stereotype.Component;

@Component
public class PersonAttributeDtoMapper {

    private final AttributeDtoMapper attributeDtoMapper;

    public PersonAttributeDtoMapper(AttributeDtoMapper attributeDtoMapper) {
        this.attributeDtoMapper = attributeDtoMapper;
    }

    public PersonAttributeDto toDto(PersonAttribute personAttribute) {
        return new PersonAttributeDto(
                personAttribute.getId(),
                attributeDtoMapper.toDto(personAttribute.getAttribute()),
                personAttribute.getValue(),
                personAttribute.getValidFrom(),
                personAttribute.getValidTo(),
                Boolean.TRUE.equals(personAttribute.isPendingDelete()));
    }

    public ReducedPersonAttributeDto toReducedDto(PersonAttribute personAttribute) {
        return new ReducedPersonAttributeDto(
                personAttribute.getId(),
                attributeDtoMapper.toDto(personAttribute.getAttribute()),
                personAttribute.getValue());
    }

    public PersonAttributeLiteDto toLiteDto(PersonAttribute personAttribute) {
        return new PersonAttributeLiteDto(
                personAttribute.getId(),
                attributeDtoMapper.toDto(personAttribute.getAttribute()),
                personAttribute.getValue(),
                personAttribute.getPerson().getId());
    }

    public PersonAttribute patchDtoToModel(PersonAttributePatch dto) {
        return new PersonAttribute.Builder()
                .withId(dto.id())
                .withValue(dto.value())
                .build();
    }
}
