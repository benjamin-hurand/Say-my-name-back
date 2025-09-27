// src/main/java/com/saymyname/webapp/dto/subscription/SubscribeBulkRequestDto.java
package com.saymyname.webapp.dto.subscription;

import java.util.List;

/** Body pour POST /bulk */
public record SubscribeBulkRequestDto(List<Long> personIds) {
}
