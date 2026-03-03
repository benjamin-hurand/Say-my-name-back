// src/main/java/com/saymyname/webapp/mapper/subscription/UserSubscriptionDtoMapper.java
package com.saymyname.webapp.mapper.subscription;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.UserSubscription;
import com.saymyname.webapp.dto.subscription.UserSubscriptionDto;

@Component
public class UserSubscriptionDtoMapper {

    public UserSubscriptionDto toDto(UserSubscription m) {
        return new UserSubscriptionDto(m.getUserId(), m.getPersonId(), m.getCreatedAt());
    }

    public UserSubscription toModel(UserSubscriptionDto dto) {
        return UserSubscription.builder()
                .userId(dto.userId())
                .personId(dto.personId())
                .createdAt(dto.createdAt())
                .build();
    }
}
