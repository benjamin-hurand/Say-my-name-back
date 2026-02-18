package com.saymyname.core.model.organization;

import com.saymyname.core.model.enums.OrgType;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Organization {
    Long id;
    String orgKey;
    String name;
    OrgType orgType;
    boolean active;
    Instant createdAt;
    Instant updatedAt;
}