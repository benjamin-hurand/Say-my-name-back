package com.saymyname.core.model.people;

import com.saymyname.core.model.enums.tenant.ScopeKind;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Fact {
    Long id;
    ScopeKind scopeKind;
    Long workspaceId;
    Long teamId;
    Long personId;
    Long attributeId;
    String value;
    Instant validFrom;
    Instant validTo;
    boolean deleted;

    // Backward-compatible accessor used by legacy code paths.
    public Attribute getAttribute() {
        return attributeId == null ? null : Attribute.builder().id(attributeId).build();
    }
}
