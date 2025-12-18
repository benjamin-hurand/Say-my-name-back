// src/main/java/com/saymyname/core/events/invitation/InvitationCreatedEvent.java
package com.saymyname.core.events.invitation;

public record InvitationCreatedEvent(
        Long invitationId,
        String rawToken, // non stocké en BDD : on le passe via l’événement
        String rawPin, // idem
        String email,
        String constraintsJson // pour locale/message…
) {
}
