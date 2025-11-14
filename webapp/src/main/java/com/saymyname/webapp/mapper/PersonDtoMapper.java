package com.saymyname.webapp.mapper;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.service.attribute.AttributeMetaCache;
import com.saymyname.service.photo.PhotoUrlResolver;
import com.saymyname.webapp.dto.PersonDto;
import com.saymyname.webapp.dto.changerequest.PersonSummaryDto;

@Component
public class PersonDtoMapper {

        private final PersonAttributeDtoMapper personAttributeDtoMapper;
        private final UserDtoMapper userDtoMapper;
        private final PhotoDtoMapper photoDtoMapper;
        private final PhotoUrlResolver photoUrlResolver;
        private final AttributeMetaCache attributeMetaCache;

        public PersonDtoMapper(PersonAttributeDtoMapper personAttributeDtoMapper, UserDtoMapper userDtoMapper,
                        PhotoDtoMapper photoDtoMapper, PhotoUrlResolver photoUrlResolver,
                        AttributeMetaCache attributeMetaCache) {
                this.personAttributeDtoMapper = personAttributeDtoMapper;
                this.userDtoMapper = userDtoMapper;
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
                                userDtoMapper.toDto(person.getUser(), organizationRole),
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
                                userDtoMapper.toDto(person.getUser()),
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
                String displayName = buildCurrentDisplayName(person);
                String photoUrl = person.getApprovedPhoto()
                                .map(p -> photoUrlResolver.smallUrl(p.getStorageKey()))
                                .orElse(null);
                return new PersonSummaryDto(displayName, photoUrl);
        }

        /** Actif si validFrom ≤ date < validTo et non pendingDelete. */
        private static boolean isActiveAt(PersonAttribute a, LocalDateTime at) {
                if (a == null || a.isPendingDelete())
                        return false;
                var from = a.getValidFrom();
                var to = a.getValidTo();
                if (from != null && at.isBefore(from))
                        return false;
                if (to != null && !at.isBefore(to))
                        return false;
                return true;
        }

        /** Construit le displayName à partir des primaires actifs maintenant. */
        private String buildCurrentDisplayName(Person person) {
                LocalDateTime now = LocalDateTime.now();
                return person.getAttributes().stream()
                                .filter(a -> a.getAttribute() != null && a.getAttribute().getId() != null)
                                .filter(a -> isActiveAt(a, now))
                                .filter(a -> attributeMetaCache.isPrimary(a.getAttribute().getId()))
                                .sorted(Comparator.comparingInt(
                                                a -> attributeMetaCache.displayOrder(a.getAttribute().getId())))
                                .map(PersonAttribute::getValue)
                                .filter(v -> v != null && !v.isBlank())
                                .collect(Collectors.joining(" "))
                                .trim();
        }
}
