package com.saymyname.persistence.mapper.leaderboard;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.leaderboard.XpEvent;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.leaderboard.XpEventEntity;
import com.saymyname.persistence.mapper.UserEntityMapper;

@Component
public class XpEventEntityMapper {

    @Autowired
    public XpEventEntityMapper(UserEntityMapper userMapper) {
    }

    public XpEventEntity toEntity(XpEvent model) {
        if (model == null)
            return null;

        XpEventEntity e = XpEventEntity.builder().build();
        e.setId(model.getId());

        if (model.getUserId() != null) {
            e.setUser(new UserEntity(model.getUserId()));
        } else {
            e.setUser(null);
        }

        e.setEventId(model.getEventId());
        e.setEventKey(model.getEventKey());
        e.setSourceType(model.getSourceType() != null ? model.getSourceType() : "SYSTEM");
        e.setSourceId(model.getSourceId());
        e.setDeltaXp(model.getDeltaXp());
        e.setCreatedAt(toLocalDateTime(model.getCreatedAt()));
        return e;
    }

    public XpEvent toModel(XpEventEntity e) {
        if (e == null)
            return null;

        return XpEvent.builder()
                .id(e.getId())
                .userId(e.getUser() != null ? e.getUser().getId() : null)
                .eventId(e.getEventId())
                .eventKey(e.getEventKey())
                .sourceType(e.getSourceType())
                .sourceId(e.getSourceId())
                .deltaXp(e.getDeltaXp())
                .createdAt(toInstant(e.getCreatedAt()))
                .build();
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
