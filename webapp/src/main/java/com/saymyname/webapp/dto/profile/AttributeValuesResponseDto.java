// src/main/java/com/saymyname/webapp/dto/profile/AttributeValuesResponseDto.java
package com.saymyname.webapp.dto.profile;

import java.util.List;
import com.saymyname.webapp.dto.FactDto;

public record AttributeValuesResponseDto(
                Long attributeId,
                List<FactDto> values // état canonique après écriture (actives + futures non-pending)
) {
}
