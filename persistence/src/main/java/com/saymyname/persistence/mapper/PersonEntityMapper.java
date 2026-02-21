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
        private final FactEntityMapper factEntityMapper;

        @Autowired
        public PersonEntityMapper(PhotoEntityMapper photoEntityMapper,
                        FactEntityMapper factEntityMapper) {
                this.photoEntityMapper = photoEntityMapper;
                this.factEntityMapper = factEntityMapper;
        }

        // --- Entity ⇐ Model
        public PersonEntity toEntity(Person person) {
                if (person == null) {
                        return null;
                }

                PersonEntity personEntity = new PersonEntity();
                personEntity.setId(person.getId());

                // Facts
                if (person.getFacts() != null) {
                        personEntity.setFacts(
                                        person.getFacts().stream()
                                                        .map(factEntityMapper::toEntity)
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
                                .withFacts(
                                                personEntity.getFacts() != null
                                                                ? personEntity.getFacts().stream()
                                                                                .map(factEntityMapper::toModel)
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
