package com.saymyname.persistence.mapper;

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
                .createdAt(e.getCreatedAt())
                .build();
    }

    public UserSubscriptionEntity toEntity(UserSubscription m) {
        if (m == null)
            return null;
        var id = new UserSubscriptionId(m.getUserId(), m.getPersonId());
        // createdAt géré par la DB (DEFAULT CURRENT_TIMESTAMP)
        return new UserSubscriptionEntity(id, null);
    }
}
