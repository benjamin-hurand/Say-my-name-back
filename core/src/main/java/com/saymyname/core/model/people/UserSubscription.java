package com.saymyname.core.model.people;

import java.time.Instant;
import java.util.Objects;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class UserSubscription {

    Long userId;
    Long personId;

    Instant createdAt;

    public static UserSubscription of(Long userId, Long personId) {
        return UserSubscription.builder()
                .userId(userId)
                .personId(personId)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserSubscription that))
            return false;
        return Objects.equals(userId, that.userId)
                && Objects.equals(personId, that.personId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, personId);
    }
}