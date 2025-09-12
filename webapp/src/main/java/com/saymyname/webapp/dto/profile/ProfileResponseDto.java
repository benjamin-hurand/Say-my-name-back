package com.saymyname.webapp.dto.profile;

import java.util.List;

import com.saymyname.webapp.dto.PersonDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestSummaryDto;

public record ProfileResponseDto(
        PersonDto person,
        List<ChangeRequestSummaryDto> changeRequests) {

}
