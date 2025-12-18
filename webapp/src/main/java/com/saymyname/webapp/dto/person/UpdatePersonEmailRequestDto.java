// src/main/java/com/saymyname/webapp/dto/person/UpdatePersonEmailRequest.java
package com.saymyname.webapp.dto.person;

import com.saymyname.core.model.enums.EmailKind;
import com.saymyname.core.model.enums.EmailSourceKind;

public record UpdatePersonEmailRequestDto(
                String email,
                EmailKind kind,
                EmailSourceKind sourceKind,
                String sourceLabel,
                Boolean primary,
                Boolean active) {
}
