// src/main/java/com/saymyname/service/quiz/DefaultAnswerKeyService.java
package com.saymyname.service.quiz;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.saymyname.core.model.people.Fact;
import com.saymyname.persistence.dao.FactDao;

@Service
public class DefaultAnswerKeyService implements AnswerKeyService {

    private final FactDao factDao;

    public DefaultAnswerKeyService(FactDao factDao) {
        this.factDao = Objects.requireNonNull(factDao, "factDao");
    }

    @Override
    public String compute(Long personId, Long targetAttributeId) {
        if (personId == null || personId <= 0) {
            throw new IllegalArgumentException("personId is required");
        }
        if (targetAttributeId == null || targetAttributeId <= 0) {
            throw new IllegalArgumentException("targetAttributeId is required");
        }

        List<Fact> facts = factDao.findAttributesByPersonId(personId);
        if (facts == null || facts.isEmpty()) {
            return "";
        }

        // Mono-attribute: on prend la première valeur non vide correspondante.
        // Si plusieurs Facts existent (legacy), on choisit la première rencontrée
        // (stable si DAO stable).
        String value = facts.stream()
                .filter(f -> f != null
                        && f.getAttribute() != null
                        && Objects.equals(f.getAttribute().getId(), targetAttributeId))
                .map(f -> f.getValue() == null ? "" : f.getValue().trim())
                .filter(v -> !v.isEmpty())
                .findFirst()
                .orElse(null);

        return value;
    }
}