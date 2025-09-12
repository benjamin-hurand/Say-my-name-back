package com.saymyname.persistence.mapper;

import com.saymyname.core.model.common.User;
import com.saymyname.core.model.enums.ChangeStatus;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.ChangeRequest;
import com.saymyname.core.model.people.ChangeRequestItem;
import com.saymyname.core.model.people.Person;
import com.saymyname.persistence.entity.AttributeEntity;
import com.saymyname.persistence.entity.ChangeRequestEntity;
import com.saymyname.persistence.entity.ChangeRequestItemEntity;
import com.saymyname.persistence.entity.PersonEntity;
import com.saymyname.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChangeRequestEntityMapper {

    private final PersonEntityMapper personEntityMapper;
    private final UserEntityMapper userEntityMapper;
    private final ChangeRequestItemEntityMapper itemEntityMapper;
    private final AttributeEntityMapper attributeEntityMapper;

    public ChangeRequestEntityMapper(PersonEntityMapper personEntityMapper,
            UserEntityMapper userEntityMapper,
            ChangeRequestItemEntityMapper itemEntityMapper,
            AttributeEntityMapper attributeEntityMapper) {
        this.personEntityMapper = personEntityMapper;
        this.userEntityMapper = userEntityMapper;
        this.itemEntityMapper = itemEntityMapper;
        this.attributeEntityMapper = attributeEntityMapper;
    }

    /*
     * ===========================
     * MODEL -> ENTITY
     * ===========================
     */
    public ChangeRequestEntity toEntity(ChangeRequest m) {
        if (m == null)
            return null;

        ChangeRequestEntity e = new ChangeRequestEntity();
        e.setId(m.getId());

        // Réfs "id-only" pour éviter un SELECT
        if (m.getPerson() != null && m.getPerson().getId() != null) {
            PersonEntity personRef = new PersonEntity();
            personRef.setId(m.getPerson().getId());
            e.setPerson(personRef);
        }
        if (m.getRequester() != null && m.getRequester().getId() != null) {
            UserEntity requesterRef = new UserEntity();
            requesterRef.setId(m.getRequester().getId());
            e.setRequester(requesterRef);
        }

        if (m.getAttribute() != null && m.getAttribute().getId() != null) {
            AttributeEntity attributeRef = new AttributeEntity();
            attributeRef.setId(m.getAttribute().getId());
            e.setAttribute(attributeRef);
        }

        // Motif global
        e.setRequestReason(m.getRequestReason());

        // Statut & audit
        e.setStatus(m.getStatus() != null ? m.getStatus() : ChangeStatus.PENDING);
        e.setCreatedAt(m.getCreatedAt()); // @PrePersist fera le défaut si null
        e.setUpdatedAt(m.getUpdatedAt());
        if (m.getResolvedBy() != null && m.getResolvedBy().getId() != null) {
            UserEntity rbRef = new UserEntity();
            rbRef.setId(m.getResolvedBy().getId());
            e.setResolvedBy(rbRef);
        }
        e.setResolvedAt(m.getResolvedAt());
        e.setResolutionComment(m.getResolutionComment());

        // Items: map et recoller la relation inverse
        if (m.getItems() != null) {
            List<ChangeRequestItemEntity> items = m.getItems().stream()
                    .map(itemEntityMapper::toEntity)
                    .collect(Collectors.toList());
            e.setItems(items);
            if (items != null) {
                for (ChangeRequestItemEntity it : items) {
                    it.setChangeRequest(e);
                }
            }
        }

        return e;
    }

    /*
     * ===========================
     * ENTITY -> MODEL
     * ===========================
     */
    public ChangeRequest toModel(ChangeRequestEntity e) {
        if (e == null)
            return null;

        Person personModel = (e.getPerson() != null)
                ? personEntityMapper.toShortModel(e.getPerson())
                : null;

        User requesterModel = (e.getRequester() != null)
                ? userEntityMapper.toShortModel(e.getRequester())
                : null;

        Attribute attributeModel = (e.getAttribute() != null)
                ? attributeEntityMapper.toShortModel(e.getAttribute())
                : null;

        User resolvedByModel = (e.getResolvedBy() != null)
                ? userEntityMapper.toShortModel(e.getResolvedBy())
                : null;

        List<ChangeRequestItem> items = (e.getItems() != null)
                ? e.getItems().stream().map(itemEntityMapper::toModel).collect(Collectors.toList())
                : null;

        return new ChangeRequest.Builder()
                .withId(e.getId())
                .withPerson(personModel)
                .withRequester(requesterModel)
                .withAttribute(attributeModel)
                .withRequestReason(e.getRequestReason())
                .withStatus(e.getStatus() != null ? e.getStatus() : ChangeStatus.PENDING)
                .withCreatedAt(e.getCreatedAt())
                .withUpdatedAt(e.getUpdatedAt())
                .withResolvedBy(resolvedByModel)
                .withResolvedAt(e.getResolvedAt())
                .withResolutionComment(e.getResolutionComment())
                .withItems(items)
                .build();
    }

    /** Modèle “léger” (id only) pour éviter des charges profondes. */
    public ChangeRequest toShortModel(ChangeRequestEntity e) {
        if (e == null)
            return null;
        return new ChangeRequest.Builder()
                .withId(e.getId())
                .build();
    }
}
