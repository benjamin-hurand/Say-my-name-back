package com.saymyname.core.model.people;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class UserSubscription {
    Long userId;
    Long personId;
    Instant createdAt;
}