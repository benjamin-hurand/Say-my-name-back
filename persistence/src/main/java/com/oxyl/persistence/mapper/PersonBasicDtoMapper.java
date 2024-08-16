package com.oxyl.persistence.mapper;

import com.oxyl.core.model.people.Person;
import com.oxyl.persistence.dto.PersonBasicDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PersonBasicDtoMapper {
    public PersonBasicDto toDto(Person person) {
        return new PersonBasicDto(person.getId(), person.getFirstName(), person.getLastName());
    }

    public Person toGameModel(PersonBasicDto personBasicDto) {
        return new Person.Builder()
                .withId(personBasicDto.getId())
                .withFirstName(personBasicDto.getFirstName())
                .withLastName(personBasicDto.getLastName())
                .build();
    }

    public List<Person> toGameModelList(List<PersonBasicDto> personBasicDto) {
        return personBasicDto.stream().map(this::toGameModel).collect(Collectors.toList());
    }
}
