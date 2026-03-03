package com.saymyname.persistence.mapper;

import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.Fact;
import com.saymyname.persistence.entity.organization.attribute.AttributeEntity;
import com.saymyname.persistence.entity.organization.FactEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FactEntityMapper {

    private final AttributeEntityMapper attributeEntityMapper;

    @Autowired
    public FactEntityMapper(AttributeEntityMapper attributeEntityMapper) {
        this.attributeEntityMapper = attributeEntityMapper;
    }

    public FactEntity toEntity(Fact fact) {
        if (fact == null) {
            return null;
        }

        // Référence PersonEntity minimaliste (id only) pour éviter un fetch
        PersonEntity personRef = null;
        Long personId = null;
        Person personModel = fact.getPerson();
        if (personModel != null && personModel.getId() != null) {
            personRef = new PersonEntity();
            personRef.setId(personModel.getId());
            personId = personModel.getId();
        }

        AttributeEntity attributeEntity = attributeEntityMapper.toEntity(fact.getAttribute());
        Long attributeId = attributeEntity != null ? attributeEntity.getId() : null;

        FactEntity entity = new FactEntity();
        entity.setId(fact.getId());
        entity.setAttribute(attributeEntity);
        entity.setAttributeId(attributeId);
        entity.setPerson(personRef);
        entity.setPersonId(personId);
        entity.setValue(fact.getValue());
        entity.setValidFrom(fact.getValidFrom());
        entity.setValidTo(fact.getValidTo());
        entity.setDeleted(fact.isDeleted());

        return entity;
    }

    public Fact toModel(FactEntity factEntity) {
        if (factEntity == null) {
            return null;
        }

        return new Fact.Builder()
                .withId(factEntity.getId())
                .withAttribute(attributeEntityMapper.toModel(factEntity.getAttribute()))
                .withValue(factEntity.getValue())
                .withValidFrom(factEntity.getValidFrom())
                .withValidTo(factEntity.getValidTo())
                .withDeleted(factEntity.isDeleted())
                .build();
    }

    public Fact toShortModel(FactEntity factEntity) {
        if (factEntity == null) {
            return null;
        }
        return new Fact.Builder()
                .withId(factEntity.getId())
                .build();
    }

    public Fact toFullModel(FactEntity factEntity) {
        if (factEntity == null) {
            return null;
        }

        Person personModel = null;
        if (factEntity.getPerson() != null) {
            personModel = new Person.Builder()
                    .withId(factEntity.getPerson().getId())
                    .build();
        }

        return new Fact.Builder()
                .withId(factEntity.getId())
                .withAttribute(attributeEntityMapper.toModel(factEntity.getAttribute()))
                .withValue(factEntity.getValue())
                .withValidFrom(factEntity.getValidFrom())
                .withValidTo(factEntity.getValidTo())
                .withDeleted(factEntity.isDeleted())
                .withPerson(personModel)
                .build();
    }
}
