package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.Person;
import com.saymyname.webapp.dto.ReducedPersonDto;

@Component
public class ReducedPersonDtoMapper {
    public ReducedPersonDto toDto(Person person) {
        return new ReducedPersonDto(person.getId());
    }

    public Person toModel(ReducedPersonDto personDto) {
        return Person.builder()
                .id(personDto.id())
                .build();
    }
}
