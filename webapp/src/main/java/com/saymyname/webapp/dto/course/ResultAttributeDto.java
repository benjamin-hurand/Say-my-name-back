// src/main/java/com/saymyname/webapp/dto/course/ResultAttributeDto.java
package com.saymyname.webapp.dto.course;

public record ResultAttributeDto(
                Long attributeId,
                String attributeName,
                String value,
                boolean correct,
                boolean target) {
}
