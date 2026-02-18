package com.saymyname.core.model.people;

import com.saymyname.core.model.enums.ChangeRequestStatus;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ChangeRequestListQuery {
    Integer page;
    Integer size;
    List<ChangeRequestStatus> statuses;
    Long personId;
    Long submittedByUserId;
    Long attributeId;
    String action;
    String sort;
    String q;
    Instant from;
    Instant to;
}
