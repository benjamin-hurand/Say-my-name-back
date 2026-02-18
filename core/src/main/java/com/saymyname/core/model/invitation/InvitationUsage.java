package com.saymyname.core.model.invitation;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class InvitationUsage {
    Long id;
    Long invitationId;
    Long userId;
    Long personId;
    Instant usedAt;
    byte[] usedIp;
    String userAgent;
}