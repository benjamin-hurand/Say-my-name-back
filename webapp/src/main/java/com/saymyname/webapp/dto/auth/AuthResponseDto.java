// src/main/java/com/saymyname/webapp/dto/auth/AuthResponseDto.java
package com.saymyname.webapp.dto.auth;

public record AuthResponseDto(
        String accessToken,
        SessionDto session) {
}
