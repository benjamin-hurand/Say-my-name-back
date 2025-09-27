// src/main/java/com/saymyname/webapp/dto/subscription/UserSubscriptionDto.java
package com.saymyname.webapp.dto.subscription;

import java.time.Instant;

public record UserSubscriptionDto(Long userId, Long personId, Instant createdAt) {
}
