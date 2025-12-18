// src/main/java/com/saymyname/webapp/dto/invitation/AcceptInvitationRequest.java
package com.saymyname.webapp.dto.invitation;

public record AcceptInvitationRequest(
        String token,
        String pin, // optionnel
        Long personId // optionnel (lier à une Person existante)
) {
}
