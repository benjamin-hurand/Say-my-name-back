// src/main/java/com/saymyname/webapp/mapper/person/PersonEmailDtoMapper.java
package com.saymyname.webapp.mapper.person;

import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.PersonEmail;
import com.saymyname.webapp.dto.person.CreatePersonEmailRequestDto;
import com.saymyname.webapp.dto.person.PersonEmailDto;
import com.saymyname.webapp.dto.person.UpdatePersonEmailRequestDto;

@Component
public class PersonEmailDtoMapper {

    // ---------- DTO -> Model ----------

    public PersonEmail toModelForCreate(Long personId, CreatePersonEmailRequestDto dto) {
        if (dto == null)
            return null;

        PersonEmail m = new PersonEmail();
        m.setEmail(safeTrim(dto.email()));
        m.setKind(dto.kind());
        m.setSourceKind(dto.sourceKind());
        m.setSourceLabel(safeTrim(dto.sourceLabel()));
        m.setPrimary(Boolean.TRUE.equals(dto.primary()));
        m.setActive(true); // par défaut à la création

        if (personId != null) {
            Person p = new Person();
            p.setId(personId);
            m.setPerson(p);
        }
        return m;
    }

    public PersonEmail toModelForUpdate(Long personId, Long emailId, UpdatePersonEmailRequestDto dto) {
        if (dto == null)
            return null;

        PersonEmail m = new PersonEmail();
        m.setId(emailId);
        m.setEmail(safeTrim(dto.email()));
        m.setKind(dto.kind());
        m.setSourceKind(dto.sourceKind());
        m.setSourceLabel(safeTrim(dto.sourceLabel()));
        if (dto.primary() != null)
            m.setPrimary(dto.primary());
        if (dto.active() != null)
            m.setActive(dto.active());

        if (personId != null) {
            Person p = new Person();
            p.setId(personId);
            m.setPerson(p);
        }
        return m;
    }

    // ---------- Model -> DTO ----------

    public PersonEmailDto toResponse(PersonEmail m) {
        if (m == null)
            return null;
        return new PersonEmailDto(
                m.getId(),
                m.getPerson() != null ? m.getPerson().getId() : null,
                m.getEmail(),
                m.getKind(),
                m.getSourceKind(),
                m.getSourceLabel(),
                m.isPrimary(),
                m.isActive(),
                m.getVerifiedAt(),
                m.getBouncedAt(),
                m.getCreatedAt(),
                m.getUpdatedAt());
    }

    public List<PersonEmailDto> toResponseList(List<PersonEmail> list) {
        return list == null ? List.of() : list.stream().map(this::toResponse).toList();
    }

    // ---------- Helpers ----------

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}
