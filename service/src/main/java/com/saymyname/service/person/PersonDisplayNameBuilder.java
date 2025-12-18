// src/main/java/com/saymyname/service/person/PersonDisplayNameBuilder.java
package com.saymyname.service.person;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.service.attribute.AttributeMetaCache;

@Component
public class PersonDisplayNameBuilder {

    private final AttributeMetaCache attributeMetaCache;

    public PersonDisplayNameBuilder(AttributeMetaCache attributeMetaCache) {
        this.attributeMetaCache = attributeMetaCache;
    }

    public String build(Person person) {
        if (person == null || person.getAttributes() == null) {
            return "";
        }
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
}
