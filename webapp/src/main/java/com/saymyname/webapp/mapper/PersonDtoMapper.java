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

        private final PersonAttributeDtoMapper personAttributeDtoMapper;
        private final PhotoDtoMapper photoDtoMapper;
        private final PhotoUrlResolver photoUrlResolver;
        private final AttributeMetaCache attributeMetaCache;
        private final PersonDisplayNameBuilder displayNameBuilder;

        public PersonDtoMapper(PersonAttributeDtoMapper personAttributeDtoMapper,
                        PhotoDtoMapper photoDtoMapper, PhotoUrlResolver photoUrlResolver,
                        AttributeMetaCache attributeMetaCache, PersonDisplayNameBuilder displayNameBuilder) {
                this.displayNameBuilder = displayNameBuilder;
                this.personAttributeDtoMapper = personAttributeDtoMapper;
                this.photoUrlResolver = photoUrlResolver;
                this.photoDtoMapper = photoDtoMapper;
                this.attributeMetaCache = attributeMetaCache;
        }

        public Person toModel(Long personId) {
                return new Person.Builder()
                                .withId(personId)
                                .build();
        }

        public PersonDto toDto(Person person, OrgRole organizationRole) {
                return new PersonDto(
                                person.getId(),
                                person.getAttributes() != null
                                                ? person.getAttributes().stream().map(personAttributeDtoMapper::toDto)
                                                                .toList()
                                                : null,
                                person.getPhotos() != null
                                                ? person.getPhotos().stream().map(photoDtoMapper::toDto).toList()
                                                : null);
        }

        public PersonDto toDto(Person person) {
                return new PersonDto(
                                person.getId(),
                                person.getAttributes() != null
                                                ? person.getAttributes().stream().map(personAttributeDtoMapper::toDto)
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
