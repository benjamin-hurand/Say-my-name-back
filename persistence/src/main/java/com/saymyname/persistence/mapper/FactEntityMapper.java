package com.saymyname.persistence.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.tenant.ScopeKind;
import com.saymyname.core.model.people.Fact;
import com.saymyname.persistence.entity.organization.FactEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.entity.organization.attribute.AttributeEntity;

@Component
public class FactEntityMapper {

    public FactEntity toEntity(Fact fact) {
        if (fact == null) {
            return null;
        }

        FactEntity entity = FactEntity.builder().build();
        entity.setId(fact.getId());
        entity.setScopeKind(fact.getScopeKind());
        entity.setWorkspaceId(fact.getWorkspaceId());
        entity.setTeamId(fact.getTeamId());
        entity.setPerson(PersonEntity.builder().id(fact.getPersonId()).build());
        entity.setAttribute(AttributeEntity.builder().id(fact.getAttributeId()).build());
        entity.setValue(fact.getValue());
        entity.setValidFrom(toLocalDateTime(fact.getValidFrom()));
        entity.setValidTo(toLocalDateTime(fact.getValidTo()));
        entity.setDeleted(fact.isDeleted());
        return entity;
    }

    public Fact toModel(FactEntity entity) {
        if (entity == null) {
            return null;
        }

        return Fact.builder()
                .id(entity.getId())
                .scopeKind(entity.getScopeKind())
                .workspaceId(entity.getWorkspaceId())
                .teamId(entity.getTeamId())
                .personId(entity.getPerson().getId())
                .attributeId(entity.getAttribute().getId())
                .value(entity.getValue())
                .validFrom(toInstant(entity.getValidFrom()))
                .validTo(toInstant(entity.getValidTo()))
                .deleted(entity.isDeleted())
                .build();
    }

    public Fact toShortModel(FactEntity entity) {
        if (entity == null) {
            return null;
        }
        return Fact.builder()
                .id(entity.getId())
                .build();
    }

    public Fact toFullModel(FactEntity entity) {
        return toModel(entity);
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
