package com.oxyl.webapp.dto;

public record RegisterGoogleDto(
        String credential,
        String clientId,
        String select_by
) {
}
