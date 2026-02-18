package com.saymyname.core.model.workspace;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Workspace {
    Long id;
    String name;
    boolean active;
    Instant createdAt;
    Instant updatedAt;
}