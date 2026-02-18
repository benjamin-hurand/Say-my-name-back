package com.saymyname.core.events.invitation;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class InvitationCreatedEvent {
    Long invitationId;
    String rawToken;
    String rawPin;
    String email;
    String constraintsJson;
}
