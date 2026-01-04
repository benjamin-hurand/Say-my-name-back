package com.saymyname.webapp.dto.person;

import java.util.List;

import com.saymyname.webapp.dto.PersonDto;
import com.saymyname.webapp.dto.UserDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestSummaryDto;

/**
 * Détails complet d’une personne côté Admin :
 * - person (avec photos + attributes déjà dans PersonDto)
 * - changeRequests (facultatif selon query param)
 * - emails (liste des e-mails rattachés à la personne dans l’organisation
 * courante)
 */
public record AdminPersonDetailsDto(
                PersonDto person,
                UserDto linkedUser,
                List<ChangeRequestSummaryDto> changeRequests,
                List<PersonEmailDto> emails) {
}
