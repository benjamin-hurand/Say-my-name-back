// src/main/java/com/saymyname/webapp/mapper/person/PersonDirectoryDtoMapper.java
package com.saymyname.webapp.mapper.person;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.FollowFilter;
import com.saymyname.core.model.persondirectory.PersonCard;
import com.saymyname.core.model.persondirectory.PersonSearchCriteria;
import com.saymyname.service.photo.PhotoUrlResolver;
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
        PersonSearchCriteria.Builder b = new PersonSearchCriteria.Builder();

        // Filters
        List<PersonSearchCriteria.AttributeFilter> filters = new ArrayList<>();
        if (dto != null && dto.filters() != null) {
            for (var f : dto.filters()) {
                filters.add(new PersonSearchCriteria.AttributeFilter.Builder()
                        .withAttributeId(f.attributeId())
                        .withOperator(f.operator())
                        .withValues(f.values() != null ? f.values() : List.of())
                        .build());
            }
        }
        b.withFilters(filters);

        // Sort directives
        List<PersonSearchCriteria.SortDirective> sorts = new ArrayList<>();
        if (dto != null && dto.sort() != null) {
            for (var s : dto.sort()) {
                sorts.add(new PersonSearchCriteria.SortDirective.Builder()
                        .withKind(s.kind())
                        .withAttributeId(s.attributeId())
                        .withField(s.field())
                        .withDirection(s.direction())
                        .build());
            }
        }
        b.withSort(sorts);

        b.withFollowFilter(dto != null && dto.followFilter() != null ? dto.followFilter() : FollowFilter.ALL);
        b.withIncludeContextAttributes(dto != null && Boolean.TRUE.equals(dto.includeContextAttributes()));

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

        return new PersonCardDto(
                model.getIdPerson(),
                photoUrlResolver.smallUrl(model.getPhotoStorageKey()),
                photoUrlResolver.largeUrl(model.getPhotoStorageKey()),
                primaryDtos,
                model.isFollowed(),
                extraDtos);
    }
}
