// src/main/java/com/saymyname/webapp/dto/subscription/BulkUnsubscribeResponseDto.java
package com.saymyname.webapp.dto.subscription;

public record BulkUnsubscribeResponseDto(
        int requested,
        int removed,
        int notFoundOrAlready // ceux qui n’étaient pas suivis
) {
}
