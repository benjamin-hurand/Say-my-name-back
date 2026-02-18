// src/main/java/com/saymyname/persistence/mapper/auth/EmailVerificationTokenEntityMapper.java
package com.saymyname.persistence.mapper.auth;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.EmailVerificationToken;
import com.saymyname.persistence.entity.EmailVerificationTokenEntity;

@Component
public class EmailVerificationTokenEntityMapper {

    public EmailVerificationToken toModel(EmailVerificationTokenEntity e) {
        if (e == null)
            return null;

        return EmailVerificationToken.builder()
                .id(e.getId())
                .publicId(e.getPublicId())
                .userId(e.getUserId())
                .email(e.getEmail())
                .tokenHash(e.getTokenHash())
                .codeHashPhc(e.getCodeHashPhc())
                .purpose(e.getPurpose())
                .makePrimaryNow(e.isMakePrimaryNow())
                .attempts(e.getAttempts())
                .resendCount(e.getResendCount())
                .lastSentAt(e.getLastSentAt())
                .expiresAt(e.getExpiresAt())
                .consumedAt(e.getConsumedAt())
                .createdAt(e.getCreatedAt())
                .build();
    }

    public EmailVerificationTokenEntity toEntity(EmailVerificationToken m) {
        if (m == null)
            return null;

        EmailVerificationTokenEntity e = new EmailVerificationTokenEntity();
        e.setPublicId(m.getPublicId());
        e.setUserId(m.getUserId());
        e.setEmail(m.getEmail());
        e.setTokenHash(m.getTokenHash());
        e.setCodeHashPhc(m.getCodeHashPhc());
        e.setPurpose(m.getPurpose());
        e.setMakePrimaryNow(m.isMakePrimaryNow());
        e.setAttempts(m.getAttempts());
        e.setResendCount(m.getResendCount());
        e.setLastSentAt(m.getLastSentAt());
        e.setExpiresAt(m.getExpiresAt());
        e.setConsumedAt(m.getConsumedAt());
        return e;
    }
}
