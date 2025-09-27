package com.saymyname.webapp.dto.person;

import java.util.List;

public record PersonCardDto(
                Long idPerson,
                String photoSmallUrl,
                String photoLargeUrl,
                List<PersonAttributeExtraDto> primaryAttributes,
                boolean followed,
                List<PersonAttributeExtraDto> extraAttributes) {
}
