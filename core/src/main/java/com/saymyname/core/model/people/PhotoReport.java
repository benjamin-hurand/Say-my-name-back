package com.saymyname.core.model.people;

import com.saymyname.core.model.enums.PhotoReportReason;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PhotoReport {
    Long id;
    Long personId;
    Long reportedById;
    PhotoReportReason reasonType;
    String reasonText;
    Instant createdAt;
}