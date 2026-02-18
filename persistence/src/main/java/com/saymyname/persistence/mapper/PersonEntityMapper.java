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
        private final FactEntityMapper personAttributeEntityMapper;

        @Autowired
        public PersonEntityMapper(PhotoEntityMapper photoEntityMapper,
                        FactEntityMapper personAttributeEntityMapper) {
                this.photoEntityMapper = photoEntityMapper;
                this.personAttributeEntityMapper = personAttributeEntityMapper;
        }

        // --- Entity ⇐ Model
        public PersonEntity toEntity(Person person) {
                if (person == null) {
                        return null;
                }

                PersonEntity personEntity = PersonEntity.builder().build();
                personEntity.setId(person.getId());
                return personEntity;
        }

        // --- Model ⇐ Entity
        public Person toModel(PersonEntity personEntity) {
                if (personEntity == null) {
                        return null;
                }

                return Person.builder()
                                .id(personEntity.getId())
                                .build();
        }

        // --- Short model (id only)
        public Person toShortModel(PersonEntity personEntity) {
                if (personEntity == null) {
                        return null;
                }
                return Person.builder()
                                .id(personEntity.getId())
                                .build();
        }

        public PersonEntity toShortEntity(Person person) {
                if (person == null) {
                        return null;
                }
                PersonEntity personEntity = PersonEntity.builder().build();
                personEntity.setId(person.getId());
                return personEntity;
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
