// src/main/java/com/saymyname/persistence/mapper/invitation/InvitationUsageEntityMapper.java
package com.saymyname.persistence.mapper.invitation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.invitation.InvitationUsage;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.invitation.InvitationEntity;
import com.saymyname.persistence.entity.organization.invitation.InvitationUsageEntity;
import com.saymyname.persistence.mapper.PersonEntityMapper;
import com.saymyname.persistence.mapper.UserEntityMapper;

@Component
public class InvitationUsageEntityMapper {

    @Autowired
    public InvitationUsageEntityMapper(UserEntityMapper userMapper, PersonEntityMapper personMapper) {
    }

    public InvitationUsageEntity toEntity(InvitationUsage model, InvitationEntity parentInvitation) {
        if (model == null)
            return null;

        InvitationUsageEntity e = InvitationUsageEntity.builder().build();
        e.setId(model.getId());

        if (parentInvitation != null) {
            e.setInvitation(parentInvitation);
        } else if (model.getInvitationId() != null) {
            e.setInvitation(InvitationEntity.builder().id(model.getInvitationId()).build());
        }

        if (model.getUserId() != null) {
            e.setUser(UserEntity.builder().id(model.getUserId()).build());
        } else {
            e.setUser(null);
        }

        e.setPersonId(model.getPersonId());
        e.setUsedAt(toLocalDateTime(model.getUsedAt()));
        e.setUsedIp(model.getUsedIp());
        e.setUserAgent(model.getUserAgent());
        return e;
    }

    public InvitationUsage toModel(InvitationUsageEntity e) {
        if (e == null)
            return null;

        return InvitationUsage.builder()
                .id(e.getId())
                .invitationId(e.getInvitation() != null ? e.getInvitation().getId() : null)
                .userId(e.getUser() != null ? e.getUser().getId() : null)
                .personId(e.getPersonId())
                .usedAt(toInstant(e.getUsedAt()))
                .usedIp(e.getUsedIp())
                .userAgent(e.getUserAgent())
                .build();
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
