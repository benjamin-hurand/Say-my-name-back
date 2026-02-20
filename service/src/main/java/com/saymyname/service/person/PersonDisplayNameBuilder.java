// src/main/java/com/saymyname/service/person/PersonDisplayNameBuilder.java
package com.saymyname.service.person;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.Fact;
import com.saymyname.service.attribute.AttributeMetaCache;

@Component
public class PersonDisplayNameBuilder {

    private final AttributeMetaCache attributeMetaCache;

    public PersonDisplayNameBuilder(AttributeMetaCache attributeMetaCache) {
        this.attributeMetaCache = attributeMetaCache;
    }

    public String build(Person person) {
        if (person == null || person.getFacts() == null) {
            return "";
        }
        Instant now = Instant.now();
        return person.getFacts().stream()
                .filter(a -> a.getAttributeId() != null)
                .filter(a -> isActiveAt(a, now))
                .filter(a -> attributeMetaCache.isPrimary(a.getAttributeId()))
                .sorted(Comparator.comparingInt(
                        a -> attributeMetaCache.displayOrder(a.getAttributeId())))
                .map(Fact::getValue)
                .filter(v -> v != null && !v.isBlank())
                .collect(Collectors.joining(" "))
                .trim();
    }

    private static boolean isActiveAt(Fact a, Instant at) {
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
