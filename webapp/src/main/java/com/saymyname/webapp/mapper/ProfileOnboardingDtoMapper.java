// src/main/java/com/saymyname/webapp/mapper/ProfileOnboardingDtoMapper.java
package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.tenant.TenantMembership;
import com.saymyname.webapp.dto.profile.PersonLinkActionDto;
import com.saymyname.webapp.dto.profile.ProfileOnboardingDto;

@Component
public class ProfileOnboardingDtoMapper {

    public ProfileOnboardingDto toDto(TenantMembership uo) {
        if (uo == null) {
            return null;
        }

        PersonLinkActionDto create = toAction(uo.isCanCreatePerson(), uo.isCreateRequiresApproval());
        PersonLinkActionDto pick = toAction(uo.isCanPickPerson(), uo.isPickRequiresApproval());

        return new ProfileOnboardingDto(create, pick);
    }

    private PersonLinkActionDto toAction(boolean can, boolean requiresApproval) {
        if (!can) {
            return PersonLinkActionDto.DISABLED;
        }
        return requiresApproval ? PersonLinkActionDto.REQUEST : PersonLinkActionDto.DIRECT;
    }
}
