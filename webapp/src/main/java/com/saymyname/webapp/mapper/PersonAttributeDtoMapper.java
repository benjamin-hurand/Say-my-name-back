package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.webapp.dto.PersonAttributeDto;
import com.saymyname.webapp.dto.PersonAttributeLiteDto;
import com.saymyname.webapp.dto.ReducedPersonAttributeDto;
import com.saymyname.webapp.dto.person.PersonAttributeMinimalDto;
import com.saymyname.webapp.dto.profile.PersonAttributePatch;

@Component
public class PersonAttributeDtoMapper {

    private final AttributeDtoMapper attributeDtoMapper;

    public PersonAttributeDtoMapper(AttributeDtoMapper attributeDtoMapper) {
        this.attributeDtoMapper = attributeDtoMapper;
    }

    public PersonAttributeDto toDto(PersonAttribute personAttribute) {
        if (personAttribute == null) {
            return null;
        }
        return new PersonAttributeDto(
                personAttribute.getId(),
                personAttribute.getAttribute().getId(),
                personAttribute.getValue(),
                personAttribute.getValidFrom(),
                personAttribute.getValidTo(),
                Boolean.TRUE.equals(personAttribute.isPendingDelete()));
    }

    public ReducedPersonAttributeDto toReducedDto(PersonAttribute personAttribute) {
        if (personAttribute == null) {
            return null;
        }
        return new ReducedPersonAttributeDto(
                personAttribute.getId(),
                attributeDtoMapper.toDto(personAttribute.getAttribute()),
                personAttribute.getValue());
    }

    public PersonAttributeLiteDto toLiteDto(PersonAttribute personAttribute) {
        if (personAttribute == null) {
            return null;
        }
        return new PersonAttributeLiteDto(
                personAttribute.getId(),
                attributeDtoMapper.toDto(personAttribute.getAttribute()),
                personAttribute.getValue(),
                personAttribute.getPerson().getId());
    }

    public PersonAttributeMinimalDto toMinimalDto(PersonAttribute personAttribute) {
        if (personAttribute == null) {
            return null;
        }
        return new PersonAttributeMinimalDto(
                personAttribute.getId(),
                personAttribute.getValue());
    }

    public PersonAttribute patchDtoToModel(PersonAttributePatch dto) {
        return new PersonAttribute.Builder()
                .withId(dto.id())
                .withValue(dto.value())
                .build();
    }
}
