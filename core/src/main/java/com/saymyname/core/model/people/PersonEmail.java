package com.saymyname.core.model.people;

import com.saymyname.core.model.enums.EmailKind;
import com.saymyname.core.model.enums.EmailSourceKind;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PersonEmail {
    Long id;
    Long personId;
    String email;
    EmailKind kind;
    EmailSourceKind sourceKind;
    String sourceLabel;
    boolean primary;
    boolean active;
    Instant verifiedAt;
    Instant bouncedAt;
    Instant createdAt;
    Instant updatedAt;

    // Backward-compatible accessor for legacy code paths.
    public Person getPerson() {
        return personId == null ? null : Person.builder().id(personId).build();
    }
}
