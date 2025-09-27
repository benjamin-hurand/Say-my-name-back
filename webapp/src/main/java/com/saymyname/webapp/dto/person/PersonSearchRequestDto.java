// src/main/java/com/saymyname/webapp/dto/person/PersonSearchRequestDto.java
package com.saymyname.webapp.dto.person;

import java.util.List;

import com.saymyname.core.model.enums.FollowFilter;

public record PersonSearchRequestDto(
        List<AttributeFilterDto> filters, // filtre sur attributs avec filter=true
        List<SortDirectiveDto> sort, // tri sur attributs (sort=true) ou champs techniques
        FollowFilter followFilter,
        Boolean includeContextAttributes // si true => inclure les attributs de contexte

) {
}
