// src/main/java/com/saymyname/webapp/dto/person/PersonSearchRequestDto.java
package com.saymyname.webapp.dto.person;

import java.util.List;

public record AdminPersonSearchRequestDto(
        List<AttributeFilterDto> filters, // filtre sur attributs avec filter=true
        List<SortDirectiveDto> sort, // tri sur attributs (sort=true) ou champs techniques
        Boolean includeContextAttributes // si true => inclure les attributs de contexte

) {
}
