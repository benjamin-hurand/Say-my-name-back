package com.saymyname.webapp.dto.profile;

public record CreatePersonAttributeRequest(
        Long attributeId,
        String value) {
}