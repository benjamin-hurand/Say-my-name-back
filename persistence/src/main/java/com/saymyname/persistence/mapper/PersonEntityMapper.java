package com.saymyname.persistence.mapper;

import com.saymyname.core.model.people.Person;
import com.saymyname.persistence.entity.PersonEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PersonEntityMapper {
    private final PhotoEntityMapper photoEntityMapper;
    private final UserEntityMapper userEntityMapper;
    private final PersonAttributeEntityMapper personAttributeEntityMapper;
    private static final Logger logger = LogManager.getLogger(PersonEntityMapper.class);


    @Autowired
    public PersonEntityMapper(PhotoEntityMapper photoEntityMapper,
                              UserEntityMapper userEntityMapper,
                              PersonAttributeEntityMapper personAttributeEntityMapper) {
        this.photoEntityMapper = photoEntityMapper;
        this.userEntityMapper = userEntityMapper;
        this.personAttributeEntityMapper = personAttributeEntityMapper;
    }

    public PersonEntity toEntity(Person person) {
        if (person == null) return null;
        PersonEntity personEntity = new PersonEntity();
            personEntity.setId(person.getId());
            personEntity.setFirstName(person.getFirstName());
            personEntity.setLastName(person.getLastName());
            personEntity.setphoto(photoEntityMapper.toEntity(person.getPhoto()));
            personEntity.setUser(userEntityMapper.toEntity(person.getUser()));
            personEntity.setAttributes(person.getAttributes().stream().map(personAttributeEntityMapper::toEntity).collect(Collectors.toList()));
        return personEntity;
    }

    public Person toModel(PersonEntity personEntity) {
        if (personEntity == null) return null;
        return new Person.Builder()
                .withId(personEntity.getId())
                .withFirstName(personEntity.getFirstName())
                .withLastName(personEntity.getLastName())
                .withPhoto(photoEntityMapper.toModel((personEntity.getphoto())))
                .withUser(userEntityMapper.toModel(personEntity.getUser()))
                .withAttributes(personEntity.getAttributes().stream().map(personAttributeEntityMapper::toModel).collect(Collectors.toList()))
                .build();
    }

    public Person toShortModel(PersonEntity personEntity) {
        if (personEntity == null) return null;
        return new Person.Builder()
                .withId(personEntity.getId())
                .build();
    }

    public List<Person> toModelList(List<PersonEntity> personEntities) {
        return personEntities.stream().map(this::toModel).collect(Collectors.toList());
    }

    public List<PersonEntity> toEntityList(List<Person> persons) {
        return persons.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
