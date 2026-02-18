// src/main/java/com/saymyname/webapp/mapper/person/PersonDirectoryDtoMapper.java
package com.saymyname.webapp.mapper.person;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.FollowFilter;
import com.saymyname.core.model.persondirectory.AdminPersonCard;
import com.saymyname.core.model.persondirectory.AdminPersonSearchCriteria;
import com.saymyname.core.model.persondirectory.PersonCard;
import com.saymyname.core.model.persondirectory.PersonSearchCriteria;
import com.saymyname.service.photo.PhotoUrlResolver;
import com.saymyname.webapp.dto.person.AdminPersonCardDto;
import com.saymyname.webapp.dto.person.AdminPersonSearchRequestDto;
import com.saymyname.webapp.dto.person.PersonAttributeExtraDto;
import com.saymyname.webapp.dto.person.PersonCardDto;
import com.saymyname.webapp.dto.person.PersonSearchRequestDto;

@Component
public class PersonDirectoryDtoMapper {

    private final PhotoUrlResolver photoUrlResolver;

    public PersonDirectoryDtoMapper(PhotoUrlResolver photoUrlResolver) {
        this.photoUrlResolver = photoUrlResolver;
    }

    /** DTO -> Model (avec Builders) */
    public PersonSearchCriteria toModel(PersonSearchRequestDto dto) {
        PersonSearchCriteria.Builder b = PersonSearchCriteria.builder();

        // Filters
        List<PersonSearchCriteria.AttributeFilter> filters = new ArrayList<>();
        if (dto != null && dto.filters() != null) {
            for (var f : dto.filters()) {
                filters.add(new PersonSearchCriteria.AttributeFilter.Builder()
                        .attributeId(f.attributeId())
                        .operator(f.operator())
                        .values(f.values() != null ? f.values() : List.of())
                        .build());
            }
        }
        b.filters(filters);

        // Sort directives
        List<PersonSearchCriteria.SortDirective> sorts = new ArrayList<>();
        if (dto != null && dto.sort() != null) {
            for (var s : dto.sort()) {
                sorts.add(new PersonSearchCriteria.SortDirective.Builder()
                        .kind(s.kind())
                        .attributeId(s.attributeId())
                        .field(s.field())
                        .direction(s.direction())
                        .build());
            }
        }
        b.sort(sorts);

        b.followFilter(dto != null && dto.followFilter() != null ? dto.followFilter() : FollowFilter.ALL);
        b.includeContextAttributes(dto != null && Boolean.TRUE.equals(dto.includeContextAttributes()));

        return b.build();
    }

    /** DTO -> Model (avec Builders) */
    public AdminPersonSearchCriteria toAdminModel(AdminPersonSearchRequestDto dto) {
        AdminPersonSearchCriteria.Builder b = AdminPersonSearchCriteria.builder();

        // Filters
        List<AdminPersonSearchCriteria.AttributeFilter> filters = new ArrayList<>();
        if (dto != null && dto.filters() != null) {
            for (var f : dto.filters()) {
                filters.add(new AdminPersonSearchCriteria.AttributeFilter.Builder()
                        .attributeId(f.attributeId())
                        .operator(f.operator())
                        .values(f.values() != null ? f.values() : List.of())
                        .build());
            }
        }
        b.filters(filters);

        // Sort directives
        List<AdminPersonSearchCriteria.SortDirective> sorts = new ArrayList<>();
        if (dto != null && dto.sort() != null) {
            for (var s : dto.sort()) {
                sorts.add(new AdminPersonSearchCriteria.SortDirective.Builder()
                        .kind(s.kind())
                        .attributeId(s.attributeId())
                        .field(s.field())
                        .direction(s.direction())
                        .build());
            }
        }
        b.sort(sorts);

        b.includeContextAttributes(dto != null && Boolean.TRUE.equals(dto.includeContextAttributes()));

        return b.build();
    }

    /** Model -> DTO (primaires allégés + extras) */
    public PersonCardDto toDto(PersonCard model) {
        Objects.requireNonNull(model, "PersonCard model must not be null");

        // Primary attributes (rich -> light)
        List<PersonAttributeExtraDto> primaryDtos = new ArrayList<>();
        if (model.getPrimaryAttributes() != null) {
            model.getPrimaryAttributes().forEach(a -> {
                primaryDtos.add(new PersonAttributeExtraDto(
                        a.getAttributeId(),
                        a.getValue(),
                        a.getDisplayOrder()));
            });
        }

        // Extra/context attributes
        List<PersonAttributeExtraDto> extraDtos = new ArrayList<>();
        if (model.getExtraAttributes() != null) {
            model.getExtraAttributes().forEach(e -> {
                extraDtos.add(new PersonAttributeExtraDto(
                        e.getAttributeId(),
                        e.getValue(),
                        e.getDisplayOrder()));
            });
        }

        // displayName construit à partir des primaires
        String displayName = buildDisplayNameFromPrimary(primaryDtos);

        return new PersonCardDto(
                model.getIdPerson(),
                photoUrlResolver.smallUrl(model.getPhotoStorageKey()),
                photoUrlResolver.largeUrl(model.getPhotoStorageKey()),
                displayName,
                primaryDtos,
                model.isFollowed(),
                extraDtos);
    }

    /** Model -> DTO (primaires allégés + extras) */
    public AdminPersonCardDto toDto(AdminPersonCard model) {
        Objects.requireNonNull(model, "PersonCard model must not be null");

        // Primary attributes (rich -> light)
        List<PersonAttributeExtraDto> primaryDtos = new ArrayList<>();
        if (model.getPrimaryAttributes() != null) {
            model.getPrimaryAttributes().forEach(a -> {
                primaryDtos.add(new PersonAttributeExtraDto(
                        a.getAttributeId(),
                        a.getValue(),
                        a.getDisplayOrder()));
            });
        }

        // Extra/context attributes
        List<PersonAttributeExtraDto> extraDtos = new ArrayList<>();
        if (model.getExtraAttributes() != null) {
            model.getExtraAttributes().forEach(e -> {
                extraDtos.add(new PersonAttributeExtraDto(
                        e.getAttributeId(),
                        e.getValue(),
                        e.getDisplayOrder()));
            });
        }

        // displayName construit à partir des primaires
        String displayName = buildDisplayNameFromPrimary(primaryDtos);

        return new AdminPersonCardDto(
                model.getIdPerson(),
                photoUrlResolver.smallUrl(model.getPhotoStorageKey()),
                photoUrlResolver.largeUrl(model.getPhotoStorageKey()),
                displayName,
                primaryDtos,
                extraDtos,
                model.getEmailStatus(),
                model.isHasPendingChangeRequests());
    }

    // ---------------------------------------
    // Helper interne pour le displayName
    // ---------------------------------------
    private String buildDisplayNameFromPrimary(List<PersonAttributeExtraDto> primaryAttributes) {
        if (primaryAttributes == null || primaryAttributes.isEmpty()) {
            return "";
        }

        return primaryAttributes.stream()
                .map(PersonAttributeExtraDto::value)
                .filter(v -> v != null && !v.isBlank())
                .collect(Collectors.joining(" "))
                .trim();
    }
}
