package com.saymyname.webapp.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.UserEmail;
import com.saymyname.webapp.dto.UserEmailDto;

@Component
public class UserEmailDtoMapper {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ===== Model -> DTO =====
    public UserEmailDto toDto(UserEmail m) {
        if (m == null)
            return null;
        return new UserEmailDto(
                m.getId(),
                m.getEmail(),
                Boolean.TRUE.equals(m.isPrimary()),
                Boolean.TRUE.equals(m.isLoginAllowed()),
                Boolean.TRUE.equals(m.isRecoveryAllowed()),
                toIso(m.getVerifiedAt()),
                toIso(m.getAddedAt()),
                toIso(m.getRecoveryEligibleAt()),
                toIso(m.getUpdatedAt()));
    }

    public List<UserEmailDto> toDtoList(List<UserEmail> list) {
        return (list == null || list.isEmpty())
                ? List.of()
                : list.stream().map(this::toDto).toList();
    }

    // ===== DTO -> Model =====
    public UserEmail toModel(UserEmailDto dto) {
        if (dto == null)
            return null;
        return UserEmail.builder()
                .id(dto.id())
                .email(dto.email())
                .primary(dto.primary())
                .loginAllowed(dto.loginAllowed())
                .recoveryAllowed(dto.recoveryAllowed())
                .verifiedAt(parse(dto.verifiedAt()))
                .addedAt(parse(dto.addedAt()))
                .recoveryEligibleAt(parse(dto.recoveryEligibleAt()))
                .updatedAt(parse(dto.updatedAt()))
                .build();
    }

    public List<UserEmail> toModelList(List<UserEmailDto> list) {
        return (list == null || list.isEmpty())
                ? List.of()
                : list.stream().map(this::toModel).toList();
    }

    // ===== Helpers =====
    private static String toIso(LocalDateTime dt) {
        return dt == null ? null : dt.format(ISO);
    }

    private static LocalDateTime parse(String s) {
        return (s == null || s.isBlank()) ? null : LocalDateTime.parse(s, ISO);
    }
}
