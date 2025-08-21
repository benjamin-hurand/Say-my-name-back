package com.saymyname.persistence.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.core.model.game.options.GameAttributeFilter;
import com.saymyname.core.model.game.options.GameOptions;
import com.saymyname.persistence.entity.PersonAttributeEntity;
import com.saymyname.persistence.entity.PersonEntity;
import com.saymyname.persistence.entity.PhotoEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
@Transactional(readOnly = true)
public class PersonRepositoryCustomImpl implements PersonRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<PersonEntity> findByOptions(GameOptions options) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<PersonEntity> cq = cb.createQuery(PersonEntity.class);
        Root<PersonEntity> person = cq.from(PersonEntity.class);

        // Récupérer la photo APPROVED
        person.fetch("photos", JoinType.INNER);
        Join<PersonEntity, PhotoEntity> photoJoin = person.join("photos", JoinType.INNER);

        // Liste des prédicats de filtrage
        List<Predicate> filterPredicates = new ArrayList<>();
        filterPredicates.add(cb.equal(photoJoin.get("status"), PhotoStatus.APPROVED));
        if (options.getFilters() != null) {
            for (GameAttributeFilter filter : options.getFilters()) {
                // Créer un join INNER sur les attributs
                Join<PersonEntity, PersonAttributeEntity> filterJoin = person.join("attributes", JoinType.INNER);

                // Vérifier que l'attribut correspond (par son ID)
                Predicate attributeMatch = cb.equal(
                        filterJoin.get("attribute").get("id"),
                        filter.getAttribute().getId());

                // Condition sur la valeur : comprise entre minValue (incluse) et
                // nextValue(maxValue) (exclusive)
                String minValue = filter.getMinValue();
                String maxValue = filter.getMaxValue();
                Predicate lowerBound = cb.greaterThanOrEqualTo(filterJoin.get("value"), minValue);
                Predicate upperBound = cb.lessThan(filterJoin.get("value"), nextValue(maxValue));
                Predicate valueBetween = cb.and(lowerBound, upperBound);

                // Ajouter les conditions de validité sur l'attribut
                Predicate validFromPredicate = cb.lessThanOrEqualTo(filterJoin.get("validFrom"), cb.currentTimestamp());
                Predicate validToPredicate = cb.or(cb.isNull(filterJoin.get("validTo")),
                        cb.greaterThanOrEqualTo(filterJoin.get("validTo"), cb.currentTimestamp()));
                Predicate validPredicate = cb.and(validFromPredicate, validToPredicate);

                // Combiner toutes les conditions pour ce filtre
                filterPredicates.add(cb.and(attributeMatch, valueBetween, validPredicate));
            }
        }

        if (!filterPredicates.isEmpty()) {
            cq.where(cb.and(filterPredicates.toArray(new Predicate[0])));
        }

        // Utiliser DISTINCT pour éviter les doublons dus aux joins multiples
        cq.select(person).distinct(true);

        return entityManager.createQuery(cq).getResultList();
    }

    private String nextValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.length() == 1) {
            char c = value.charAt(0);
            // Si c'est 'Z' ou 'z', renvoyer une borne supérieure qui capture tous les cas
            if (c == 'Z' || c == 'z') {
                return value.equals("Z") ? "Z\uffff" : "z\uffff";
            } else {
                return String.valueOf((char) (c + 1));
            }
        }
        // Pour des chaînes plus longues, ajouter un caractère de fin (ici, un caractère
        // maximum)
        return value + "\uffff";
    }

}
