package com.saymyname.core.model.auth;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class UserEmail {
    Long id;
    Long userId;
    String email;
    boolean primary;
    boolean loginAllowed;
    boolean recoveryAllowed;
    Instant verifiedAt;
    Instant addedAt;
    Instant updatedAt;
    Instant recoveryEligibleAt;
}
