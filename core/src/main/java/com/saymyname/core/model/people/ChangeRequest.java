package com.saymyname.core.model.people;

import com.saymyname.core.model.enums.ChangeRequestStatus;
import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ChangeRequest {
    Long id;
    Person person;
    Long requesterId;
    Long attributeId;
    String requestReason;
    ChangeRequestStatus status;
    Instant createdAt;
    Instant updatedAt;
    Long resolvedById;
    Instant resolvedAt;
    String resolutionComment;
    @Builder.Default
    List<ChangeRequestItem> items = List.of();
}
