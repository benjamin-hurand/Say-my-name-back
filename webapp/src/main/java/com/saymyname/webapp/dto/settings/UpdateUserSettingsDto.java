package com.saymyname.webapp.dto.settings;

import org.springframework.lang.Nullable;

import com.saymyname.core.model.enums.SrsAlgorithm;

public record UpdateUserSettingsDto(
                @Nullable SrsAlgorithm srsAlgorithm) {
}