package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.people.Person;
import com.saymyname.service.attribute.AttributeMetaCache;
import com.saymyname.service.person.PersonDisplayNameBuilder;
import com.saymyname.service.photo.PhotoUrlResolver;
import com.saymyname.webapp.dto.PersonDto;
import com.saymyname.webapp.dto.changerequest.PersonSummaryDto;

@Component
public class PersonDtoMapper {

        private final FactDtoMapper personAttributeDtoMapper;
        private final PhotoDtoMapper photoDtoMapper;
        private final PhotoUrlResolver photoUrlResolver;
        // TODO: Soit remettre soit supprimer completement METACACHE
        private final AttributeMetaCache attributeMetaCache;
        private final PersonDisplayNameBuilder displayNameBuilder;

        public PersonDtoMapper(FactDtoMapper personAttributeDtoMapper,
                        PhotoDtoMapper photoDtoMapper, PhotoUrlResolver photoUrlResolver,
                        AttributeMetaCache attributeMetaCache, PersonDisplayNameBuilder displayNameBuilder) {
                this.displayNameBuilder = displayNameBuilder;
                this.personAttributeDtoMapper = personAttributeDtoMapper;
                this.photoUrlResolver = photoUrlResolver;
                this.photoDtoMapper = photoDtoMapper;
                this.attributeMetaCache = attributeMetaCache;
        }

        public Person toModel(Long personId) {
                return Person.builder()
                                .id(personId)
                                .build();
        }

        public PersonDto toDto(Person person, OrgRole organizationRole) {
                return new PersonDto(
                                person.getId(),
                                person.getFacts() != null
                                                ? person.getFacts().stream().map(personAttributeDtoMapper::toDto)
                                                                .toList()
                                                : null,
                                person.getPhotos() != null
                                                ? person.getPhotos().stream().map(photoDtoMapper::toDto).toList()
                                                : null);
        }

        public PersonDto toDto(Person person) {
                return new PersonDto(
                                person.getId(),
                                person.getFacts() != null
                                                ? person.getFacts().stream().map(personAttributeDtoMapper::toDto)
                                                                .toList()
                                                : null,
                                person.getPhotos() != null
                                                ? person.getPhotos().stream().map(photoDtoMapper::toDto).toList()
                                                : null);
        }

        /** Résumé léger (displayName actuel + photo approved si dispo). */
        public PersonSummaryDto toSummaryDto(Person person) {
                String displayName = displayNameBuilder.build(person);
                String photoUrl = person.getApprovedPhoto()
                                .map(p -> photoUrlResolver.smallUrl(p.getStorageKey()))
                                .orElse(null);
                return new PersonSummaryDto(displayName, photoUrl);
        }
}
