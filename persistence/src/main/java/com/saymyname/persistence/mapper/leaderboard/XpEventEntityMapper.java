package com.saymyname.persistence.mapper.leaderboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.leaderboard.XpEvent;
import com.saymyname.persistence.entity.organization.leaderboard.XpEventEntity;
import com.saymyname.persistence.mapper.UserEntityMapper;

@Component
public class XpEventEntityMapper {

    private final UserEntityMapper userMapper;

    @Autowired
    public XpEventEntityMapper(UserEntityMapper userMapper) {
        this.userMapper = userMapper;
    }

    public XpEventEntity toEntity(XpEvent model) {
        if (model == null)
            return null;

        XpEventEntity e = new XpEventEntity();
        e.setId(model.getId());
        e.setUser(userMapper.toEntity(model.getUser()));

        e.setEventId(model.getEventId());
        e.setEventKey(model.getEventKey());

        e.setSourceType(model.getSourceType() != null ? model.getSourceType() : "SYSTEM");
        e.setSourceId(model.getSourceId());

        e.setDeltaXp(model.getDeltaXp());

        // createdAt géré par @PrePersist côté entity.
        return e;
    }

    public XpEvent toModel(XpEventEntity e) {
        if (e == null)
            return null;

        return XpEvent.builder()
                .id(e.getId())
                .user(userMapper.toShortModel(e.getUser()))
                .eventId(e.getEventId())
                .eventKey(e.getEventKey())
                .sourceType(e.getSourceType())
                .sourceId(e.getSourceId())
                .deltaXp(e.getDeltaXp())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
