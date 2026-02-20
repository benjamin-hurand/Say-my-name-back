// src/main/java/com/saymyname/persistence/mapper/auth/EmailVerificationTokenEntityMapper.java
package com.saymyname.persistence.mapper.auth;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.EmailVerificationToken;
import com.saymyname.core.model.enums.EmailVerificationPurpose;
import com.saymyname.persistence.entity.EmailVerificationTokenEntity;
import com.saymyname.persistence.entity.UserEntity;

@Component
public class EmailVerificationTokenEntityMapper {

    public EmailVerificationToken toModel(EmailVerificationTokenEntity e) {
        if (e == null)
            return null;

        Long userId = (e.getUser() != null) ? e.getUser().getId() : null;

        return EmailVerificationToken.builder()
                .id(e.getId())
                .publicId(e.getPublicId())
                .userId(userId)
                .email(e.getEmail())
                .tokenHash(e.getTokenHash())
                .codeHashPhc(e.getCodeHashPhc())
                .purpose(e.getPurpose())
                .makePrimaryNow(e.isMakePrimaryNow())
                .attempts(e.getAttempts())
                .resendCount(e.getResendCount())
                .lastSentAt(toInstant(e.getLastSentAt()))
                .expiresAt(toInstant(e.getExpiresAt()))
                .consumedAt(toInstant(e.getConsumedAt()))
                .createdAt(toInstant(e.getCreatedAt()))
                .build();
    }

    public EmailVerificationTokenEntity toEntity(EmailVerificationToken m) {
        if (m == null)
            return null;

        EmailVerificationTokenEntity e = EmailVerificationTokenEntity.builder().build();
        e.setPublicId(m.getPublicId());

        if (m.getUserId() != null) {
            e.setUser(new UserEntity(m.getUserId()));
        } else {
            e.setUser(null);
        }

        e.setEmail(m.getEmail());
        e.setTokenHash(m.getTokenHash());
        e.setCodeHashPhc(m.getCodeHashPhc());
        e.setPurpose(m.getPurpose());
        e.setMakePrimaryNow(m.isMakePrimaryNow());
        e.setAttempts(m.getAttempts());
        e.setResendCount(m.getResendCount());
        e.setLastSentAt(toLocalDateTime(m.getLastSentAt()));
        e.setExpiresAt(toLocalDateTime(m.getExpiresAt()));
        e.setConsumedAt(toLocalDateTime(m.getConsumedAt()));
        return e;
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
