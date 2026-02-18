// src/main/java/com/saymyname/webapp/dto/person/AdminPersonCardDto.java
package com.saymyname.webapp.dto.person;

import java.util.List;

import com.saymyname.core.model.enums.EmailStatus;

public record AdminPersonCardDto(
                Long idPerson,
                String photoSmallUrl,
                String photoLargeUrl,
                String displayName,
                List<FactExtraDto> primaryAttributes,
                List<FactExtraDto> extraAttributes,
                EmailStatus emailStatus,
                boolean hasPendingChangeRequests) {
}
