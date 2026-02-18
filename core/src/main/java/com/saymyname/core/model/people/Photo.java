package com.saymyname.core.model.people;

import com.saymyname.core.model.enums.PhotoStatus;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Photo {
    Long id;
    String storageKey;
    Long personId;
    PhotoStatus status;
    Instant submittedAt;
    Long submittedById;
    Instant approvedAt;
    Long approvedById;
}