package com.saymyname.persistence.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.UserSubscription;
import com.saymyname.persistence.entity.organization.subscription.UserSubscriptionEntity;

@Component
public class UserSubscriptionEntityMapper {

    public UserSubscription toModel(UserSubscriptionEntity e) {
        if (e == null)
            return null;

        return UserSubscription.builder()
                .userId(e.getUserId())
                .personId(e.getPersonId())
                .createdAt(e.getCreatedAt())
                .build();
    }

    public UserSubscriptionEntity toEntity(UserSubscription m) {
        if (m == null)
            return null;

        return UserSubscriptionEntity.builder()
                .userId(m.getUserId())
                .personId(m.getPersonId())
                .build();
    }
}