package com.saymyname.webapp.dto.settings;

import com.saymyname.core.model.enums.SrsAlgorithm;

public record UserSettingsDto(
        SrsAlgorithm srsAlgorithm) {
}
