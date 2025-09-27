// src/main/java/com/saymyname/webapp/dto/subscription/BulkSubscribeResponseDto.java
package com.saymyname.webapp.dto.subscription;

public record BulkSubscribeResponseDto(int requested, int inserted, int alreadyExisting) {
}
