package com.saymyname.webapp.mapper;

import com.saymyname.core.model.people.Person;
import com.saymyname.webapp.dto.PersonDto;
import org.springframework.stereotype.Component;

@Component
public class PersonDtoMapper {
    public PersonDto toDto(Person person) {
        return new PersonDto(person.getId());
    }

    public Person toModel(PersonDto personDto) {
        return new Person.Builder()
                .withId(personDto.id())
                .build();
    }
}
