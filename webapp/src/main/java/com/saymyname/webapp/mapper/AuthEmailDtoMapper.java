// src/main/java/com/saymyname/webapp/mapper/AuthEmailDtoMapper.java
package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.EmailVerificationChallenge;
import com.saymyname.core.model.auth.EmailVerificationConfirmation;
import com.saymyname.core.model.enums.EmailVerificationKind;
import com.saymyname.webapp.dto.UserEmailDto;
import com.saymyname.webapp.dto.auth.AddEmailResponseDto;
import com.saymyname.webapp.dto.auth.ConfirmEmailVerificationResponseDto;

@Component
public class AuthEmailDtoMapper {

    private final UserEmailDtoMapper userEmailDtoMapper;

    public AuthEmailDtoMapper(UserEmailDtoMapper userEmailDtoMapper) {
        this.userEmailDtoMapper = userEmailDtoMapper;
    }

    public AddEmailResponseDto toAddEmailResponseDto(EmailVerificationChallenge c) {
        if (c == null)
            return null;

        return new AddEmailResponseDto(
                c.getEmail(),
                c.isAlreadyAttached(),
                c.isAlreadyVerified(),
                c.getVerificationKind(), // ✅ "OTP" / "NONE"
                c.getVerificationId(),
                c.getTtlMinutes());
    }

    public ConfirmEmailVerificationResponseDto toConfirmEmailVerificationResponseDto(
            EmailVerificationConfirmation conf) {
        if (conf == null)
            return null;

        UserEmailDto emailDto = userEmailDtoMapper.toDto(conf.getEmail());

        return new ConfirmEmailVerificationResponseDto(
                emailDto,
                conf.isPrimaryChanged());
    }
}
