package com.saymyname.core.model.auth;

import com.saymyname.core.model.enums.AuthProvider;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class UserIdentity {

    Long id;
    Long userId;
    AuthProvider provider;
    String providerSubject;
    String passwordHash;
    @Builder.Default
    boolean enabled = true;
    Instant createdAt;
    Instant updatedAt;
    Instant lastUsedAt;

    public boolean isLocal() {
        return provider == AuthProvider.LOCAL;
    }

    public boolean hasPasswordHash() {
        return passwordHash != null && !passwordHash.isBlank();
    }
}
