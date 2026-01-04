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

        return new EmailVerificationToken.Builder()
                .withId(e.getId())
                .withPublicId(e.getPublicId())
                .withUserId(e.getUserId())
                .withEmail(e.getEmail())
                .withTokenHash(e.getTokenHash())
                .withCodeHashPhc(e.getCodeHashPhc())
                .withPurpose(e.getPurpose())
                .withMakePrimaryNow(e.isMakePrimaryNow())
                .withAttempts(e.getAttempts())
                .withResendCount(e.getResendCount())
                .withLastSentAt(e.getLastSentAt())
                .withExpiresAt(e.getExpiresAt())
                .withConsumedAt(e.getConsumedAt())
                .withCreatedAt(e.getCreatedAt())
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
