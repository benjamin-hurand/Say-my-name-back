package com.saymyname.persistence.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.UserSubscription;
import com.saymyname.persistence.entity.organization.subscription.UserSubscriptionEntity;
import com.saymyname.persistence.entity.organization.subscription.UserSubscriptionId;

@Component
public class UserSubscriptionEntityMapper {

    public UserSubscription toModel(UserSubscriptionEntity e) {
        if (e == null)
            return null;
        return UserSubscription.builder()
                .userId(e.getId() != null ? e.getId().getUserId() : null)
                .personId(e.getId() != null ? e.getId().getPersonId() : null)
                .createdAt(toInstant(e.getCreatedAt()))
                .build();
    }

    public UserSubscriptionEntity toEntity(UserSubscription m) {
        if (m == null)
            return null;
        UserSubscriptionId id = UserSubscriptionId.builder()
                .tenantId(null)
                .userId(m.getUserId())
                .personId(m.getPersonId())
                .build();
        return UserSubscriptionEntity.builder()
                .id(id)
                .createdAt(toLocalDateTime(m.getCreatedAt()))
                .build();
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
