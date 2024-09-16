package com.oxyl.persistence.mapper;

import com.oxyl.core.model.people.Person;
import com.oxyl.core.model.people.PersonAttribute;
import com.oxyl.persistence.entity.PersonAttributeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonAttributeEntityMapper {

    private final AttributeEntityMapper attributeEntityMapper;

    @Autowired
    public PersonAttributeEntityMapper(AttributeEntityMapper attributeEntityMapper) {
        this.attributeEntityMapper = attributeEntityMapper;
    }

    public PersonAttributeEntity toEntity(PersonAttribute personAttribute) {
        if (personAttribute == null) return null;
        return new PersonAttributeEntity(personAttribute.getId(), attributeEntityMapper.toEntity(personAttribute.getAttribute()), personAttribute.getValue());
    }

    public PersonAttribute toModel(PersonAttributeEntity personAttributeEntity) {
        if (personAttributeEntity == null) return null;
        return new PersonAttribute.Builder()
                .withId(personAttributeEntity.getId())
                .withAttribute(attributeEntityMapper.toModel(personAttributeEntity.getAttribute()))
                .withValue(personAttributeEntity.getValue())
                .build();
    }

    public PersonAttribute toFullModel(PersonAttributeEntity personAttributeEntity) {
        if (personAttributeEntity == null) return null;
        return new PersonAttribute.Builder()
                .withId(personAttributeEntity.getId())
                .withAttribute(attributeEntityMapper.toModel(personAttributeEntity.getAttribute()))
                .withValue(personAttributeEntity.getValue())
                .withPerson(new Person.Builder().withId(personAttributeEntity.getPerson().getId()).build())
                .build();
    }

}
