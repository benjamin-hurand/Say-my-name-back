// src/main/java/com/saymyname/webapp/dto/subscription/BulkBySearchResultDto.java
package com.saymyname.webapp.dto.subscription;

public record BulkBySearchResultDto(
        long matched,
        long acted,
        long skipped,
        double seconds) {
}
