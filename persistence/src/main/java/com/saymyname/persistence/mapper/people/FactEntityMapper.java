package com.saymyname.persistence.mapper.people;

import com.saymyname.core.model.people.Fact;
import com.saymyname.persistence.entity.organization.FactEntity;
import com.saymyname.persistence.mapper.AttributeEntityMapper;
import com.saymyname.persistence.mapper.PersonEntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FactEntityMapper {

    private final PersonEntityMapper personMapper;
    private final AttributeEntityMapper attributeMapper;

    @Autowired
    public FactEntityMapper(PersonEntityMapper personMapper,
            AttributeEntityMapper attributeMapper) {
        this.personMapper = personMapper;
        this.attributeMapper = attributeMapper;
    }

    public FactEntity toEntity(Fact model) {
        if (model == null)
            return null;

        FactEntity e = new FactEntity();
        e.setId(model.getId());

        e.setScopeKind(model.getScopeKind());
        e.setWorkspaceId(model.getWorkspaceId());
        e.setTeamId(model.getTeamId());

        e.setPersonId(model.getPersonId());
        e.setAttributeId(model.getAttributeId());

        e.setValue(model.getValue());
        e.setValidFrom(model.getValidFrom());
        e.setValidTo(model.getValidTo());
        e.setDeleted(model.isDeleted());

        return e;
    }

    public Fact toModel(FactEntity e) {
        if (e == null)
            return null;

        Fact.Builder b = new Fact.Builder()
                .withId(e.getId())
                .withScopeKind(e.getScopeKind())
                .withWorkspaceId(e.getWorkspaceId())
                .withTeamId(e.getTeamId())
                .withPersonId(e.getPersonId())
                .withAttributeId(e.getAttributeId())
                .withValue(e.getValue())
                .withValidFrom(e.getValidFrom())
                .withValidTo(e.getValidTo())
                .withDeleted(e.isDeleted());

        // optionnel: seulement si déjà chargé
        if (e.getPerson() != null) {
            b.withPerson(personMapper.toShortModel(e.getPerson()));
        }
        if (e.getAttribute() != null) {
            b.withAttribute(attributeMapper.toShortModel(e.getAttribute()));
        }

        return b.build();
    }
}