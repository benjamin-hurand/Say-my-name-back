package com.saymyname.core.model.people;

import com.saymyname.core.model.auth.User;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ChangeRequestResolution {
    Long changeRequestId;
    User resolver;
    String resolutionComment;
    @Builder.Default
    List<ChangeRequestResolutionItem> decisions = List.of();
}
