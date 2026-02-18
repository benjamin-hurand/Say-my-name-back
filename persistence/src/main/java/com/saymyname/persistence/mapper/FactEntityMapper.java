package com.saymyname.persistence.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.ScopeKind;
import com.saymyname.core.model.people.Fact;
import com.saymyname.persistence.entity.organization.FactEntity;

@Component
public class FactEntityMapper {

    public FactEntity toEntity(Fact fact) {
        if (fact == null) {
            return null;
        }

        FactEntity entity = FactEntity.builder().build();
        entity.setId(fact.getId());
        entity.setScopeKind(toEntityScopeKind(fact.getScopeKind()));
        entity.setWorkspaceId(fact.getWorkspaceId());
        entity.setTeamId(fact.getTeamId());
        entity.setPersonId(fact.getPersonId());
        entity.setAttributeId(fact.getAttributeId());
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
                .scopeKind(toModelScopeKind(entity.getScopeKind()))
                .workspaceId(entity.getWorkspaceId())
                .teamId(entity.getTeamId())
                .personId(entity.getPersonId())
                .attributeId(entity.getAttributeId())
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

    private FactEntity.ScopeKind toEntityScopeKind(ScopeKind value) {
        return value == null ? null : FactEntity.ScopeKind.valueOf(value.name());
    }

    private ScopeKind toModelScopeKind(FactEntity.ScopeKind value) {
        return value == null ? null : ScopeKind.valueOf(value.name());
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
