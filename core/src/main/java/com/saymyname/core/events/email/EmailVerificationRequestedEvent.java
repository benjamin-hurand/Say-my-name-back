// src/main/java/com/saymyname/core/events/email/EmailVerificationRequestedEvent.java
package com.saymyname.core.events.email;

public record EmailVerificationRequestedEvent(
        Long userId,
        String email,
        String rawToken,
        String code,
        String purpose) {
}
