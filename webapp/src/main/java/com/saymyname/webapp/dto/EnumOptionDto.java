// src/main/java/com/saymyname/webapp/dto/EnumOptionDto.java
package com.saymyname.webapp.dto;

public record EnumOptionDto(
        String code,
        String label,
        Integer orderIndex,
        Boolean active) {
}
