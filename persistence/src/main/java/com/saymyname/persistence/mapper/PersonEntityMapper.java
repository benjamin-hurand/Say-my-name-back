package com.saymyname.persistence.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.Person;
import com.saymyname.persistence.entity.organization.PersonEntity;

@Component
public class PersonEntityMapper {

        private final PhotoEntityMapper photoEntityMapper;
        private final UserEntityMapper userEntityMapper;
        private final PersonAttributeEntityMapper personAttributeEntityMapper;

        @Autowired
        public PersonEntityMapper(PhotoEntityMapper photoEntityMapper,
                        UserEntityMapper userEntityMapper,
                        PersonAttributeEntityMapper personAttributeEntityMapper) {
                this.photoEntityMapper = photoEntityMapper;
                this.userEntityMapper = userEntityMapper;
                this.personAttributeEntityMapper = personAttributeEntityMapper;
        }

        // --- Entity ⇐ Model
        public PersonEntity toEntity(Person person) {
                if (person == null) {
                        return null;
                }
                PersonEntity personEntity = new PersonEntity();
                personEntity.setId(person.getId());
                personEntity.setUser(userEntityMapper.toEntity(person.getUser()));

                // Attributes
                if (person.getAttributes() != null) {
                        personEntity.setAttributes(
                                        person.getAttributes().stream()
                                                        .map(personAttributeEntityMapper::toEntity)
                                                        .collect(Collectors.toList()));
                }

                // Photos
                if (person.getPhotos() != null) {
                        personEntity.setPhotos(
                                        person.getPhotos().stream()
                                                        .map(photoEntityMapper::toEntity)
                                                        .collect(Collectors.toList()));
                }

                return personEntity;
        }

        // --- Model ⇐ Entity
        public Person toModel(PersonEntity personEntity) {
                if (personEntity == null) {
                        return null;
                }
                return new Person.Builder()
                                .withId(personEntity.getId())
                                .withUser(userEntityMapper.toShortModel(personEntity.getUser()))
                                .withAttributes(
                                                personEntity.getAttributes() != null
                                                                ? personEntity.getAttributes().stream()
                                                                                .map(personAttributeEntityMapper::toModel)
                                                                                .collect(Collectors.toList())
                                                                : List.of())
                                .withPhotos(
                                                personEntity.getPhotos() != null
                                                                ? personEntity.getPhotos().stream()
                                                                                .map(photoEntityMapper::toModel)
                                                                                .collect(Collectors.toList())
                                                                : List.of())
                                .build();
        }

        public Person toModelWithMediumUser(PersonEntity personEntity) {
                if (personEntity == null) {
                        return null;
                }
                return new Person.Builder()
                                .withId(personEntity.getId())
                                .withUser(userEntityMapper.toMediumModel(personEntity.getUser()))
                                .withAttributes(
                                                personEntity.getAttributes() != null
                                                                ? personEntity.getAttributes().stream()
                                                                                .map(personAttributeEntityMapper::toModel)
                                                                                .collect(Collectors.toList())
                                                                : List.of())
                                .withPhotos(
                                                personEntity.getPhotos() != null
                                                                ? personEntity.getPhotos().stream()
                                                                                .map(photoEntityMapper::toModel)
                                                                                .collect(Collectors.toList())
                                                                : List.of())
                                .build();
        }

        // --- Short model (id only)
        public Person toShortModel(PersonEntity personEntity) {
                if (personEntity == null) {
                        return null;
                }
                return new Person.Builder()
                                .withId(personEntity.getId())
                                .build();
        }

        // --- List conversions
        public List<Person> toModelList(List<PersonEntity> personEntities) {
                return personEntities.stream()
                                .map(this::toModel)
                                .collect(Collectors.toList());
        }

        public List<PersonEntity> toEntityList(List<Person> persons) {
                return persons.stream()
                                .map(this::toEntity)
                                .collect(Collectors.toList());
        }
}
