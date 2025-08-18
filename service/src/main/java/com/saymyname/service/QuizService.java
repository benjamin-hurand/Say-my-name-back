package com.saymyname.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.saymyname.core.model.game.QuizEntry;
import com.saymyname.core.model.game.options.GameOptions;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.util.InitialCrafter;
import com.saymyname.persistence.dao.PersonDao;

@Service
public class QuizService {

    private final PersonDao personDao;
    private final InitialCrafter initialCrafter;

    public QuizService(PersonDao personDao, InitialCrafter initialCrafter) {
        this.personDao = personDao;
        this.initialCrafter = initialCrafter;
    }

    public List<QuizEntry> getQuizEntries(GameOptions options) {
        // Exemple : récupérer une liste filtrée de personnes selon vos options
        List<Person> persons = personDao.findByOptions(options);

        // Appliquer le tri en mémoire si des critères de tri sont définis
        if (options.getSortBy() != null && !options.getSortBy().isEmpty()) {
            persons.sort((p1, p2) -> {
                for (var sort : options.getSortBy()) {
                    // Extraire la valeur de l'attribut pour chaque personne
                    String val1 = getAttributeValueFor(p1, sort.getAttribute().getId());
                    String val2 = getAttributeValueFor(p2, sort.getAttribute().getId());
                    int cmp = val1.compareToIgnoreCase(val2);
                    if (cmp != 0) {
                        return "ASC".equalsIgnoreCase(sort.getOrder()) ? cmp : -cmp;
                    }
                    // Sinon, passer au critère suivant
                }
                return 0;
            });
        }

        // Pour chaque personne, récupérer la photo correspondante
        return persons.stream().map(person -> {
            String initials = initialCrafter.computeInitials(person, options.getGameMode());
            return new QuizEntry.Builder()
                    .withPersonId(person.getId())
                    .withStorageKey(person.getPhoto().getStorageKey())
                    .withInitials(initials)
                    .build();
        }).toList();
    }

    // Méthode utilitaire pour obtenir la valeur d'un attribut pour une personne
    // donnée
    private String getAttributeValueFor(Person person, Long attributeId) {
        return person.getAttributes().stream()
                .filter(attr -> attr.getAttribute().getId() == attributeId)
                .map(attr -> attr.getValue() != null ? attr.getValue() : "")
                .findFirst()
                .orElse("");
    }

}
