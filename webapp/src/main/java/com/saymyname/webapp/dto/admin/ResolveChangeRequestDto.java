// src/main/java/com/saymyname/webapp/dto/admin/ResolveChangeRequestDto.java
package com.saymyname.webapp.dto.admin;

import java.util.List;

public record ResolveChangeRequestDto(
        String resolutionComment, // note globale optionnelle
        List<ResolveChangeRequestItemDto> decisions // peut être vide, jamais null côté mapper
) {
}
