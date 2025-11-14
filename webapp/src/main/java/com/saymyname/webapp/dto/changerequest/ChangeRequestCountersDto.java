// src/main/java/com/saymyname/webapp/dto/changerequest/ChangeRequestCountersDto.java
package com.saymyname.webapp.dto.changerequest;

import java.util.Map;
import com.saymyname.core.model.enums.ChangeAction;

public record ChangeRequestCountersDto(
        int total,
        Map<ChangeAction, Integer> byAction) {
}
