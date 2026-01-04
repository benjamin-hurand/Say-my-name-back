// src/main/java/com/saymyname/webapp/dto/profile/PersonLinkActionDto.java
package com.saymyname.webapp.dto.profile;

public enum PersonLinkActionDto {
    DISABLED, // l'utilisateur ne peut pas initier l'action
    DIRECT, // action directe (pas d'approbation)
    REQUEST // initie une demande (approbation requise)
}
