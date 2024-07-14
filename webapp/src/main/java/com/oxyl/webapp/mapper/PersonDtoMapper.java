package com.oxyl.webapp.mapper;

import com.oxyl.core.model.Person;
import com.oxyl.webapp.dto.PersonDto;
import org.springframework.stereotype.Component;

@Component
public class PersonDtoMapper {
    public PersonDto toDto(Person person) {
        return new PersonDto(person.getId(), person.getFirstName(), person.getLastName());
    }

    public Person toModel(PersonDto personDto) {
        return new Person.Builder()
                .withId(personDto.id())
                .withFirstName(personDto.firstName())
                .withLastName(personDto.lastName())
                .build();
    }
}
