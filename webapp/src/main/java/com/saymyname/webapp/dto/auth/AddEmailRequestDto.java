// src/main/java/com/saymyname/webapp/dto/auth/AddEmailRequestDto.java
package com.saymyname.webapp.dto.auth;

public record AddEmailRequestDto(
        String email,
        Boolean makePrimaryNow) {
}
