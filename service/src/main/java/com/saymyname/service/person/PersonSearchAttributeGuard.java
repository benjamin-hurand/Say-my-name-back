package com.saymyname.service.person;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.AttributeCapabilities;
import com.saymyname.core.model.persondirectory.AdminPersonSearchCriteria;
import com.saymyname.core.model.persondirectory.PersonSearchCriteria;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.persistence.dao.AttributeDao;

/**
 * Server-side guard for {@code /api/persons/search} (and its admin variant):
 * a client must not be able to filter or sort by an attribute that
 * {@link AttributeCapabilities} says is not filterable/sortable, regardless
 * of what the frontend offers. The frontend cannot be trusted to enforce
 * this on its own.
 */
@Component
public class PersonSearchAttributeGuard {

    /** Id "magique" envoyé par le front pour la recherche globale texte — pas un vrai attribut. */
    private static final long GLOBAL_TEXT_ATTR_ID = -1L;

    private final AttributeDao attributeDao;

    public PersonSearchAttributeGuard(AttributeDao attributeDao) {
        this.attributeDao = attributeDao;
    }

    public void validate(PersonSearchCriteria criteria) {
        if (criteria == null) {
            return;
        }
        List<FilterRef> filters = criteria.getFilters() == null
                ? List.of()
                : criteria.getFilters().stream()
                        .map(f -> new FilterRef(f.getAttributeId()))
                        .toList();
        List<SortRef> sorts = criteria.getSort() == null
                ? List.of()
                : criteria.getSort().stream()
                        .map(s -> new SortRef(s.getKind(), s.getAttributeId()))
                        .toList();
        validateInternal(filters, sorts);
    }

    public void validateAdmin(AdminPersonSearchCriteria criteria) {
        if (criteria == null) {
            return;
        }
        List<FilterRef> filters = criteria.getFilters() == null
                ? List.of()
                : criteria.getFilters().stream()
                        .map(f -> new FilterRef(f.getAttributeId()))
                        .toList();
        List<SortRef> sorts = criteria.getSort() == null
                ? List.of()
                : criteria.getSort().stream()
                        .map(s -> new SortRef(s.getKind(), s.getAttributeId()))
                        .toList();
        validateInternal(filters, sorts);
    }

    private void validateInternal(List<FilterRef> filters, List<SortRef> sorts) {
        Set<Long> filterAttrIds = filters.stream()
                .map(FilterRef::attributeId)
                .filter(Objects::nonNull)
                .filter(id -> id != GLOBAL_TEXT_ATTR_ID)
                .collect(Collectors.toSet());

        Set<Long> sortAttrIds = sorts.stream()
                .filter(s -> "ATTRIBUTE".equalsIgnoreCase(s.kind()))
                .map(SortRef::attributeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (filterAttrIds.isEmpty() && sortAttrIds.isEmpty()) {
            return;
        }

        Set<Long> allIds = new HashSet<>(filterAttrIds);
        allIds.addAll(sortAttrIds);

        Long tenantId = TenantContext.get();
        Map<Long, Attribute> byId = attributeDao.findAllByIdsForTenant(tenantId, List.copyOf(allIds)).stream()
                .collect(Collectors.toMap(Attribute::getId, Function.identity()));

        for (Long id : filterAttrIds) {
            Attribute attribute = requireAttribute(byId, id);
            if (!AttributeCapabilities.isFilterable(attribute)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cet attribut n'est pas filtrable: " + attribute.getName());
            }
        }

        for (Long id : sortAttrIds) {
            Attribute attribute = requireAttribute(byId, id);
            if (!AttributeCapabilities.isSortable(attribute)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cet attribut n'est pas triable: " + attribute.getName());
            }
        }
    }

    private static Attribute requireAttribute(Map<Long, Attribute> byId, Long id) {
        Attribute attribute = byId.get(id);
        if (attribute == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attribut introuvable: " + id);
        }
        return attribute;
    }

    private record FilterRef(Long attributeId) {
    }

    private record SortRef(String kind, Long attributeId) {
    }
}
