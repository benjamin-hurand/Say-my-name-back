// src/main/java/com/saymyname/service/quiz/DefaultAnswerKeyService.java
package com.saymyname.service.quiz;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.persistence.dao.PersonAttributeDao;

@Service
public class DefaultAnswerKeyService implements AnswerKeyService {

    private final PersonAttributeDao personAttributeDao;

    public DefaultAnswerKeyService(PersonAttributeDao personAttributeDao) {
        this.personAttributeDao = personAttributeDao;
    }

    @Override
    public AnswerKey compute(Long personId, List<Long> targetAttributeIds, String operator) {
        List<PersonAttribute> attrs = personAttributeDao.findAttributesByPersonId(personId);

        List<String> correctValues = attrs.stream()
                .filter(pa -> targetAttributeIds.contains(pa.getAttribute().getId()))
                .map(pa -> pa.getValue() == null ? "" : pa.getValue())
                .toList();

        boolean and = operator == null || operator.isBlank()
                || operator.trim().toUpperCase(Locale.ROOT).equals("AND");

        String joined = String.join(" ", correctValues).trim();
        return new AnswerKey(and, correctValues, joined);
    }
}
