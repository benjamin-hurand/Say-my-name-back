package com.saymyname.core.model.people;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.ChangeResolutionDecision;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class BulkChangeRequestResolution {
    User resolver;
    @Builder.Default
    List<Long> changeRequestIds = List.of();
    ChangeResolutionDecision decision;
    String resolutionComment;
}
