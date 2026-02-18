package com.saymyname.core.model.invitation;

import com.saymyname.core.model.enums.InvitationType;
import com.saymyname.core.model.enums.OrgRole;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Invitation {
    Long id;
    InvitationType type;
    String label;
    String note;
    String constraintsJson;
    OrgRole role;
    String email;
    Long personId;
    byte[] tokenHash;
    String pinHashPhc;
    Integer maxUses;
    int usesCount;
    Instant expiresAt;
    Instant revokedAt;
    Long createdById;
    Instant createdAt;
    Long acceptedById;
    Instant acceptedAt;
    Instant lastUsedAt;
}