// src/main/java/com/saymyname/webapp/dto/changerequest/PersonSummaryDto.java
package com.saymyname.webapp.dto.changerequest;

public record PersonSummaryDto(
        String displayName,
        String photoUrl) {
}
