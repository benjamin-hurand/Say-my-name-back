// src/main/java/com/saymyname/webapp/mapper/AuthRegisterDtoMapper.java
package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.EmailVerificationChallenge;
import com.saymyname.webapp.dto.auth.RegisterClassicResponseDto;

@Component
public class AuthRegisterDtoMapper {

    public RegisterClassicResponseDto toRegisterClassicResponseDto(EmailVerificationChallenge c) {
        if (c == null) {
            return new RegisterClassicResponseDto(
                    null,
                    false,
                    null,
                    null,
                    null);
        }

        return new RegisterClassicResponseDto(
                c.getEmail(),
                c.isAlreadyVerified(),
                c.getVerificationKind(),
                c.getVerificationId(),
                c.getTtlMinutes());
    }
}
