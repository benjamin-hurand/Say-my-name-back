package com.saymyname.persistence.mapper;

import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.persistence.entity.organization.attribute.AttributeEntity;
import com.saymyname.persistence.entity.organization.PersonAttributeEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;

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
        if (personAttribute == null) {
            return null;
        }

        // Référence PersonEntity minimaliste (id only) pour éviter un fetch
        PersonEntity personRef = null;
        Person personModel = personAttribute.getPerson();
        if (personModel != null && personModel.getId() != null) {
            personRef = new PersonEntity();
            personRef.setId(personModel.getId());
        }

        AttributeEntity attributeEntity = attributeEntityMapper.toEntity(personAttribute.getAttribute());

        PersonAttributeEntity entity = new PersonAttributeEntity();
        entity.setId(personAttribute.getId());
        entity.setAttribute(attributeEntity);
        entity.setPerson(personRef);
        entity.setValue(personAttribute.getValue());
        entity.setValidFrom(personAttribute.getValidFrom());
        entity.setValidTo(personAttribute.getValidTo());
        entity.setPendingDelete(personAttribute.isPendingDelete());

        return entity;
    }

    public PersonAttribute toModel(PersonAttributeEntity personAttributeEntity) {
        if (personAttributeEntity == null) {
            return null;
        }

        return PersonAttribute.builder()
                .id(personAttributeEntity.getId())
                .attribute(attributeEntityMapper.toModel(personAttributeEntity.getAttribute()))
                .value(personAttributeEntity.getValue())
                .validFrom(personAttributeEntity.getValidFrom())
                .validTo(personAttributeEntity.getValidTo())
                .pendingDelete(personAttributeEntity.isPendingDelete())
                .build();
    }

    public PersonAttribute toShortModel(PersonAttributeEntity personAttributeEntity) {
        if (personAttributeEntity == null) {
            return null;
        }
        return PersonAttribute.builder()
                .id(personAttributeEntity.getId())
                .build();
    }

    public PersonAttribute toFullModel(PersonAttributeEntity personAttributeEntity) {
        if (personAttributeEntity == null) {
            return null;
        }

        Person personModel = null;
        if (personAttributeEntity.getPerson() != null) {
            personModel = Person.builder()
                    .id(personAttributeEntity.getPerson().getId())
                    .build();
        }

        return PersonAttribute.builder()
                .id(personAttributeEntity.getId())
                .attribute(attributeEntityMapper.toModel(personAttributeEntity.getAttribute()))
                .value(personAttributeEntity.getValue())
                .validFrom(personAttributeEntity.getValidFrom())
                .validTo(personAttributeEntity.getValidTo())
                .pendingDelete(personAttributeEntity.isPendingDelete())
                .person(personModel)
                .build();
    }
}
