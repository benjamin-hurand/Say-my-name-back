package com.saymyname.webapp.dto;

import java.time.LocalDateTime;

import com.saymyname.core.model.enums.PhotoStatus;

public record PhotoDto(
                Long id,
                String url,
                PhotoStatus status,
                LocalDateTime submittedAt,
                ReducedUserDto submittedBy,
                LocalDateTime approvedAt,
                ReducedUserDto approvedBy) {
}
