package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.Person;
import com.saymyname.webapp.dto.PersonDto;
import com.saymyname.webapp.dto.profile.ProfileResponseDto;

@Component
public class PersonDtoMapper {

    private final PersonAttributeDtoMapper personAttributeDtoMapper;
    private final UserDtoMapper userDtoMapper;
    private final PhotoDtoMapper photoDtoMapper;

    public PersonDtoMapper(PersonAttributeDtoMapper personAttributeDtoMapper, UserDtoMapper userDtoMapper,
            PhotoDtoMapper photoDtoMapper) {
        this.personAttributeDtoMapper = personAttributeDtoMapper;
        this.userDtoMapper = userDtoMapper;
        this.photoDtoMapper = photoDtoMapper;
    }

    public Person toModel(Long personId) {
        return new Person.Builder()
                .withId(personId)
                .build();
    }

    public PersonDto toDto(Person person) {
        return new PersonDto(
                person.getId(),
                userDtoMapper.toDto(person.getUser()),
                photoDtoMapper.toDto(person.getPhoto()),
                person.getAttributes() != null ? person.getAttributes().stream()
                        .map(personAttributeDtoMapper::toReducedDto)
                        .toList() : null);
    }

    public ProfileResponseDto toProfileResponseDto(Person person) {
        return new ProfileResponseDto(toDto(person));
    }
}
