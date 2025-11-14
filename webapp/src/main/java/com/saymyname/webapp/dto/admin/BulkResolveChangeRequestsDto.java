// src/main/java/com/saymyname/webapp/dto/admin/change/BulkResolveChangeRequestsDto.java
package com.saymyname.webapp.dto.admin;

import java.util.List;

public record BulkResolveChangeRequestsDto(
        List<Long> ids, // ids des ChangeRequest
        String decision, // "APPROVE" | "REJECT"
        String resolutionComment // optionnel, commentaire global
) {
}
