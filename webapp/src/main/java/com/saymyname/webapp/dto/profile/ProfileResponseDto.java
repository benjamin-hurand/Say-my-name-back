// src/main/java/com/saymyname/webapp/dto/profile/ProfileResponseDto.java
package com.saymyname.webapp.dto.profile;

import java.util.List;

import com.saymyname.webapp.dto.PersonDto;
import com.saymyname.webapp.dto.UserDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestSummaryDto;

public record ProfileResponseDto(
        UserDto user,
        PersonDto person,
        List<ChangeRequestSummaryDto> changeRequests,
        ProfileOnboardingDto onboarding // null si person != null
) {
}
