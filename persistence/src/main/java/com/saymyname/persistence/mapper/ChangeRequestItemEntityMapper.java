// src/main/java/com/saymyname/persistence/mapper/ChangeRequestItemEntityMapper.java
package com.saymyname.persistence.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.ChangeAction;
import com.saymyname.core.model.enums.ChangeRequestItemStatus;
import com.saymyname.core.model.people.ChangeRequestItem;
import com.saymyname.persistence.entity.organization.ChangeRequestItemEntity;
import com.saymyname.persistence.entity.organization.FactEntity;

@Component
public class ChangeRequestItemEntityMapper {

    public ChangeRequestItemEntityMapper(FactEntityMapper personAttributeEntityMapper) {
    }

    public ChangeRequestItemEntity toEntity(ChangeRequestItem m) {
        if (m == null)
            return null;

        ChangeRequestItemEntity e = ChangeRequestItemEntity.builder().build();
        e.setId(m.getId());
        e.setChangeRequestId(m.getChangeRequestId());

        ChangeAction action = m.getAction();
        e.setAction(toEntityAction(action));

        if (action == ChangeAction.CREATE) {
            e.setFact(null);
        } else if (action == ChangeAction.UPDATE || action == ChangeAction.DELETE) {
            if (m.getFactId() != null) {
                e.setFact(FactEntity.builder().id(m.getFactId()).build());
            } else {
                e.setFact(null);
            }
        } else {
            e.setFact(null);
        }

        e.setProposedValue(m.getProposedValue());
        e.setResolutionStatus(
                m.getResolutionStatus() != null ? toEntityResolutionStatus(m.getResolutionStatus())
                        : ChangeRequestItemEntity.ResolutionStatus.PENDING);
        e.setResolutionComment(m.getResolutionComment());
        return e;
    }

    public ChangeRequestItem toModel(ChangeRequestItemEntity e) {
        if (e == null)
            return null;

        return ChangeRequestItem.builder()
                .id(e.getId())
                .changeRequestId(e.getChangeRequestId())
                .action(toModelAction(e.getAction()))
                .factId(e.getFact() != null ? e.getFact().getId() : null)
                .proposedValue(e.getProposedValue())
                .resolutionStatus(
                        e.getResolutionStatus() != null ? toModelResolutionStatus(e.getResolutionStatus())
                                : ChangeRequestItemStatus.PENDING)
                .resolutionComment(e.getResolutionComment())
                .build();
    }

    public ChangeRequestItem toShortModel(ChangeRequestItemEntity e) {
        if (e == null)
            return null;
        return ChangeRequestItem.builder()
                .id(e.getId())
                .build();
    }

    private ChangeRequestItemEntity.ChangeAction toEntityAction(ChangeAction value) {
        return value == null ? null : ChangeRequestItemEntity.ChangeAction.valueOf(value.name());
    }

    private ChangeAction toModelAction(ChangeRequestItemEntity.ChangeAction value) {
        return value == null ? null : ChangeAction.valueOf(value.name());
    }

    private ChangeRequestItemEntity.ResolutionStatus toEntityResolutionStatus(ChangeRequestItemStatus value) {
        return value == null ? null : ChangeRequestItemEntity.ResolutionStatus.valueOf(value.name());
    }

    private ChangeRequestItemStatus toModelResolutionStatus(ChangeRequestItemEntity.ResolutionStatus value) {
        return value == null ? null : ChangeRequestItemStatus.valueOf(value.name());
    }
}
