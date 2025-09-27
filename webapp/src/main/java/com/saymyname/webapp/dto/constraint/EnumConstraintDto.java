// src/main/java/com/saymyname/webapp/dto/constraint/EnumConstraintDto.java
package com.saymyname.webapp.dto.constraint;

public record EnumConstraintDto(
        Boolean allowInactive,
        Boolean storeCode) {
}
