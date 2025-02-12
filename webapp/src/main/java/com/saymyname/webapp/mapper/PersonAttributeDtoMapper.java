package com.saymyname.webapp.mapper;

import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.webapp.dto.PersonAttributeDto;
import org.springframework.stereotype.Component;

@Component
public class PersonAttributeDtoMapper {

    private final AttributeDtoMapper attributeDtoMapper;
    private final PersonDtoMapper personDtoMapper;

    public PersonAttributeDtoMapper(AttributeDtoMapper attributeDtoMapper, PersonDtoMapper personDtoMapper) {
        this.attributeDtoMapper = attributeDtoMapper;
        this.personDtoMapper = personDtoMapper;
    }

    public PersonAttributeDto toDto(PersonAttribute personAttribute) {
        return new PersonAttributeDto(
                personAttribute.getId(),
                attributeDtoMapper.toDto(personAttribute.getAttribute()),
                personAttribute.getValue(),
                personDtoMapper.toDto(personAttribute.getPerson())
        );
    }
}
