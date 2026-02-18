package com.saymyname.core.events.email;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class EmailVerificationRequestedEvent {
    Long userId;
    String email;
    String rawToken;
    String code;
    String purpose;
}
