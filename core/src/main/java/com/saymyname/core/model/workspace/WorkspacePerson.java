package com.saymyname.core.model.workspace;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class WorkspacePerson {
    Long workspaceId;
    Long personId;
    Instant createdAt;
    Long addedBy;
}