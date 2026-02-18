// src/main/java/com/saymyname/persistence/mapper/invitation/InvitationUsageEntityMapper.java
package com.saymyname.persistence.mapper.invitation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.invitation.InvitationUsage;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.invitation.InvitationEntity;
import com.saymyname.persistence.entity.organization.invitation.InvitationUsageEntity;
import com.saymyname.persistence.mapper.PersonEntityMapper;
import com.saymyname.persistence.mapper.UserEntityMapper;

@Component
public class InvitationUsageEntityMapper {

    private final UserEntityMapper userMapper;
    private final PersonEntityMapper personMapper;

    @Autowired
    public InvitationUsageEntityMapper(UserEntityMapper userMapper, PersonEntityMapper personMapper) {
        this.userMapper = userMapper;
        this.personMapper = personMapper;
    }

    /**
     * Mappe un modèle vers une entité. L'association parent (invitation) peut être
     * fournie pour éviter des cycles / rechargements.
     */
    public InvitationUsageEntity toEntity(InvitationUsage model, InvitationEntity parentInvitation) {
        if (model == null)
            return null;

        InvitationUsageEntity e = new InvitationUsageEntity();
        e.setId(model.getId());

        if (parentInvitation != null) {
            e.setInvitation(parentInvitation);
        } else if (model.getInvitationId() != null) {
            // Optionnel : on peut créer un proxy pour éviter un SELECT
            InvitationEntity proxy = new InvitationEntity();
            proxy.setId(model.getInvitationId());
            e.setInvitation(proxy);
        }

        UserEntity user = userMapper.toEntity(model.getUser());
        e.setUser(user);

        // Person est nullable
        if (model.getPerson() != null) {
            PersonEntity person = personMapper.toEntity(model.getPerson());
            e.setPerson(person);
        } else {
            e.setPerson(null);
        }

        e.setUsedAt(model.getUsedAt());
        e.setUsedIp(model.getUsedIp()); // byte[] (IPv4/IPv6 en binaire)
        e.setUserAgent(model.getUserAgent());

        return e;
    }

    public InvitationUsage toModel(InvitationUsageEntity e) {
        if (e == null)
            return null;

        return InvitationUsage.builder()
                .id(e.getId())
                .invitationId(e.getInvitation() != null ? e.getInvitation().getId() : null)
                .user(userMapper.toShortModel(e.getUser()))
                .person(personMapper.toShortModel(e.getPerson()))
                .usedAt(e.getUsedAt())
                .usedIp(e.getUsedIp())
                .userAgent(e.getUserAgent())
                .build();
    }
}
