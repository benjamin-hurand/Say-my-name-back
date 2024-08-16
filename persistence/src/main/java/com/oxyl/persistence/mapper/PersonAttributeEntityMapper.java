package com.oxyl.persistence.mapper;

import com.oxyl.core.model.people.PersonAttribute;
import com.oxyl.persistence.entity.PersonAttributeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonAttributeEntityMapper {

    private final AttributeEntityMapper attributeEntityMapper; // Mapper for Attribute

    @Autowired
    public PersonAttributeEntityMapper(AttributeEntityMapper attributeEntityMapper) {
        this.attributeEntityMapper = attributeEntityMapper;
    }

    public PersonAttributeEntity toEntity(PersonAttribute personAttribute) {
        if (personAttribute == null) return null;
        return new PersonAttributeEntity(personAttribute.getId(), attributeEntityMapper.toEntity(personAttribute.getAttribute()), personAttribute.getValue());
    }

    public PersonAttribute toGameModel(PersonAttributeEntity personAttributeEntity) {
        if (personAttributeEntity == null) return null;
        return new PersonAttribute.Builder()
                .withId(personAttributeEntity.getId())
                .withAttribute(attributeEntityMapper.toModel(personAttributeEntity.getAttribute()))
                .withValue(personAttributeEntity.getValue())
                .build();
        }

}
