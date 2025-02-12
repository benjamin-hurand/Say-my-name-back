package com.saymyname.persistence.mapper;

import com.saymyname.core.model.people.Person;
import com.saymyname.persistence.dto.PersonBasicDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PersonBasicDtoMapper {
    public PersonBasicDto toDto(Person person) {
        return new PersonBasicDto(person.getId(), person.getFirstName(), person.getLastName());
    }

    public Person toModel(PersonBasicDto personBasicDto) {
        return new Person.Builder()
                .withId(personBasicDto.getId())
                .withFirstName(personBasicDto.getFirstName())
                .withLastName(personBasicDto.getLastName())
                .build();
    }

    public List<Person> toModelList(List<PersonBasicDto> personBasicDto) {
        return personBasicDto.stream().map(this::toModel).collect(Collectors.toList());
    }
}
