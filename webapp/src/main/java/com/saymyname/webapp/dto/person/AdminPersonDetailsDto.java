package com.saymyname.webapp.dto.person;

import java.util.List;

import com.saymyname.webapp.dto.PersonDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestSummaryDto;

/**
 * Détails complet d’une personne côté Admin :
 * - person (avec photos + attributes déjà dans PersonDto)
 * - changeRequests (facultatif selon query param)
 */
public record AdminPersonDetailsDto(
        PersonDto person,
        List<ChangeRequestSummaryDto> changeRequests) {
}