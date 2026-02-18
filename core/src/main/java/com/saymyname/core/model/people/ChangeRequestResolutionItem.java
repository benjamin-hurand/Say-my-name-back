package com.saymyname.core.model.people;

import com.saymyname.core.model.enums.ChangeResolutionDecision;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ChangeRequestResolutionItem {
    Long itemId;
    ChangeResolutionDecision decision;
    String resolutionComment;
}
