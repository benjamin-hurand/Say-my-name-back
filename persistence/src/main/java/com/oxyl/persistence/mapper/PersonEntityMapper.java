package com.oxyl.persistence.mapper;

import com.oxyl.core.model.people.Person;
import com.oxyl.persistence.entity.PersonEntity;
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
    private final PersonPromotionEntityMapper personPromotionEntityMapper;
    private static final Logger logger = LogManager.getLogger(PersonEntityMapper.class);


    @Autowired
    public PersonEntityMapper(PhotoEntityMapper photoEntityMapper,
                              UserEntityMapper userEntityMapper,
                              PersonAttributeEntityMapper personAttributeEntityMapper,
                              PersonPromotionEntityMapper personPromotionEntityMapper) {
        this.photoEntityMapper = photoEntityMapper;
        this.userEntityMapper = userEntityMapper;
        this.personAttributeEntityMapper = personAttributeEntityMapper;
        this.personPromotionEntityMapper = personPromotionEntityMapper;
    }

    public PersonEntity toEntity(Person person) {
        if (person == null) return null;
        PersonEntity personEntity = new PersonEntity();
            personEntity.setId(person.getId());
            personEntity.setFirstName(person.getFirstName());
            personEntity.setLastName(person.getLastName());
            personEntity.setPhotos(person.getPhotos().stream().map(photoEntityMapper::toEntity).collect(Collectors.toList()));
            personEntity.setUser(userEntityMapper.toEntity(person.getUser()));
            personEntity.setAttributes(person.getAttributes().stream().map(personAttributeEntityMapper::toEntity).collect(Collectors.toList()));
            personEntity.setPromotions(person.getPromotions().stream().map(personPromotionEntityMapper::toEntity).collect(Collectors.toList()));
        return personEntity;
    }

    public Person toGameModel(PersonEntity personEntity) {
        if (personEntity == null) return null;
        logger.info("Promotions de toGameModel : {}",personEntity.getPromotions().stream().map(personPromotionEntityMapper::toGameModel).collect(Collectors.toList()));
        return new Person.Builder()
                .withId(personEntity.getId())
                .withFirstName(personEntity.getFirstName())
                .withLastName(personEntity.getLastName())
                .withPhotos(personEntity.getPhotos().stream().map(photoEntityMapper::toGameModel).collect(Collectors.toList()))
                .withUser(userEntityMapper.toGameModel(personEntity.getUser()))
                .withAttributes(personEntity.getAttributes().stream().map(personAttributeEntityMapper::toModel).collect(Collectors.toList()))
                .withPromotions(personEntity.getPromotions().stream().map(personPromotionEntityMapper::toGameModel).collect(Collectors.toList()))
                .build();
    }

    public Person toShortModel(PersonEntity personEntity) {
        if (personEntity == null) return null;
        return new Person.Builder()
                .withId(personEntity.getId())
                .build();
    }

    public List<Person> toGameModelList(List<PersonEntity> personEntities) {
        return personEntities.stream().map(this::toGameModel).collect(Collectors.toList());
    }

    public List<PersonEntity> toEntityList(List<Person> persons) {
        return persons.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
