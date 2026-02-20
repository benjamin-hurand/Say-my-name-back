package com.saymyname.core.model.people;

import com.saymyname.core.model.enums.ChangeAction;
import com.saymyname.core.model.enums.ChangeRequestItemStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ChangeRequestItem {
    Long id;
    Long changeRequestId;
    ChangeAction action;
    Long factId;
    String proposedValue;
    ChangeRequestItemStatus resolutionStatus;
    String resolutionComment;
}
