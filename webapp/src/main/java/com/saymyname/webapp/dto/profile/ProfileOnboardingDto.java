// src/main/java/com/saymyname/webapp/dto/profile/ProfileOnboardingDto.java
package com.saymyname.webapp.dto.profile;

public record ProfileOnboardingDto(
        PersonLinkActionDto createPerson,
        PersonLinkActionDto pickPerson) {
}
