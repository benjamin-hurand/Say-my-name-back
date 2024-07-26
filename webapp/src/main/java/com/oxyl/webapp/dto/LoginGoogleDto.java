package com.oxyl.webapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginGoogleDto(
        @JsonProperty("credential") String credential,
        @JsonProperty("clientId") String clientId,
        @JsonProperty("select_by") String select_by
) {
}


