package com.saymyname.webapp.mapper;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.people.Fact;
import com.saymyname.core.model.people.Person;
import com.saymyname.service.photo.PhotoUrlResolver;
import com.saymyname.core.model.people.ConceptCodes;
import com.saymyname.webapp.dto.PersonDto;
import com.saymyname.webapp.dto.changerequest.PersonSummaryDto;

@Component
public class PersonDtoMapper {

        private final FactDtoMapper factDtoMapper;
        private final PhotoDtoMapper photoDtoMapper;
        private final PhotoUrlResolver photoUrlResolver;

        public PersonDtoMapper(FactDtoMapper factDtoMapper,
                        PhotoDtoMapper photoDtoMapper, PhotoUrlResolver photoUrlResolver) {
                this.factDtoMapper = factDtoMapper;
                this.photoUrlResolver = photoUrlResolver;
                this.photoDtoMapper = photoDtoMapper;
        }

        public Person toModel(Long personId) {
                return new Person.Builder()
                                .withId(personId)
                                .build();
        }

        public PersonDto toDto(Person person, OrgRole organizationRole) {
                return new PersonDto(
                                person.getId(),
                                resolveDisplayName(person, LocalDateTime.now()),
                                person.getFacts() != null
                                                ? person.getFacts().stream().map(factDtoMapper::toDto)
                                                                .toList()
                                                : null,
                                person.getPhotos() != null
                                                ? person.getPhotos().stream().map(photoDtoMapper::toDto).toList()
                                                : null);
        }

        public PersonDto toDto(Person person) {
                return new PersonDto(
                                person.getId(),
                                resolveDisplayName(person, LocalDateTime.now()),
                                person.getFacts() != null
                                                ? person.getFacts().stream().map(factDtoMapper::toDto)
                                                                .toList()
                                                : null,
                                person.getPhotos() != null
                                                ? person.getPhotos().stream().map(photoDtoMapper::toDto).toList()
                                                : null);
        }

        public PersonSummaryDto toSummaryDto(Person person) {
                LocalDateTime now = LocalDateTime.now();
                String displayName = resolveDisplayName(person, now);
                String photoUrl = person != null ? person.getApprovedPhoto()
                                .map(p -> photoUrlResolver.smallUrl(p.getStorageKey()))
                                .orElse(null) : null;
                return new PersonSummaryDto(displayName, photoUrl);
        }

        private static String resolveDisplayName(Person person, LocalDateTime at) {
                if (person == null || person.getFacts() == null) {
                        return "";
                }

                return person.getFacts().stream()
                                .filter(Objects::nonNull)
                                .filter(f -> f.getAttribute() != null)
                                .filter(f -> ConceptCodes.IDENTITY.equals(f.getAttribute().getConceptCode()))
                                .filter(f -> isActiveAt(f, at))
                                .map(Fact::getValue)
                                .filter(v -> v != null && !v.isBlank())
                                .map(String::trim)
                                .findFirst()
                                .orElse("");
        }

        private static boolean isActiveAt(Fact a, LocalDateTime at) {
                if (a == null || a.isDeleted())
                        return false;
                var from = a.getValidFrom();
                var to = a.getValidTo();
                if (from != null && at.isBefore(from))
                        return false;
                if (to != null && !at.isBefore(to))
                        return false;
                return true;
        }

}
