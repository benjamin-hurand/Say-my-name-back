package com.saymyname.webapp.dto.person;

import java.util.List;

public record AdminPersonCardDto(
                Long idPerson,
                String photoSmallUrl,
                String photoLargeUrl,
                List<PersonAttributeExtraDto> primaryAttributes,
                List<PersonAttributeExtraDto> extraAttributes,
                boolean hasPendingChangeRequests) {
}
