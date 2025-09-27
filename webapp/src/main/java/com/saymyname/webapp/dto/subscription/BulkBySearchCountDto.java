// src/main/java/com/saymyname/webapp/dto/subscription/BulkBySearchCountDto.java
package com.saymyname.webapp.dto.subscription;

public record BulkBySearchCountDto(
        long totalMatches,
        long alreadyFollowed,
        long notFollowed) {
}
