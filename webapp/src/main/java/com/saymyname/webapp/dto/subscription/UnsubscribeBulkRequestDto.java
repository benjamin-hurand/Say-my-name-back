// src/main/java/com/saymyname/webapp/dto/subscription/UnsubscribeBulkRequestDto.java
package com.saymyname.webapp.dto.subscription;

import java.util.List;

public record UnsubscribeBulkRequestDto(List<Long> personIds) {
}
