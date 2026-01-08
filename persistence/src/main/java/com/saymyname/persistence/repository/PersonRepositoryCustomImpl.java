package com.saymyname.persistence.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.enums.FollowFilter;
import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.core.model.quiz.options.GameAttributeFilter;
import com.saymyname.core.model.quiz.options.GameOptions;
import com.saymyname.persistence.entity.organization.PersonAttributeEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.entity.organization.PhotoEntity;
import com.saymyname.persistence.entity.organization.subscription.UserSubscriptionEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

@Repository
@Transactional(readOnly = true)
public class PersonRepositoryCustomImpl implements PersonRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Recherche des personnes selon les options de jeu.
     * 
     * @param options options de filtrage/scope
     * @param userId  identifiant de l'utilisateur connecté (requis si
     *                populationScope = FOLLOWED ou UNFOLLOWED)
     */
    @Override
    public List<PersonEntity> findByOptions(GameOptions options, Long userId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<PersonEntity> cq = cb.createQuery(PersonEntity.class);
        Root<PersonEntity> person = cq.from(PersonEntity.class);

        // Récupérer la photo APPROVED
        person.fetch("photos", JoinType.INNER);
        Join<PersonEntity, PhotoEntity> photoJoin = person.join("photos", JoinType.INNER);

        // Liste des prédicats de filtrage
        List<Predicate> filterPredicates = new ArrayList<>();
        filterPredicates.add(cb.equal(photoJoin.get("status"), PhotoStatus.APPROVED));

        // Filtrage par attributs dynamiques
        if (options.getFilters() != null) {
            for (GameAttributeFilter filter : options.getFilters()) {
                // Join INNER sur les attributs
                Join<PersonEntity, PersonAttributeEntity> filterJoin = person.join("attributes", JoinType.INNER);

                // Attribut ciblé (par ID)
                Predicate attributeMatch = cb.equal(
                        filterJoin.get("attribute").get("id"),
                        filter.getAttribute().getId());

                // Valeur comprise entre [minValue ; nextValue(maxValue)[
                String minValue = filter.getMinValue();
                String maxValue = filter.getMaxValue();
                Predicate lowerBound = cb.greaterThanOrEqualTo(filterJoin.get("value"), minValue);
                Predicate upperBound = cb.lessThan(filterJoin.get("value"), nextValue(maxValue));
                Predicate valueBetween = cb.and(lowerBound, upperBound);

                // Validité temporelle de l'attribut
                Predicate validFromPredicate = cb.lessThanOrEqualTo(filterJoin.get("validFrom"), cb.currentTimestamp());
                Predicate validToPredicate = cb.or(
                        cb.isNull(filterJoin.get("validTo")),
                        cb.greaterThanOrEqualTo(filterJoin.get("validTo"), cb.currentTimestamp()));
                Predicate validPredicate = cb.and(validFromPredicate, validToPredicate);

                // Combinaison pour ce filtre
                filterPredicates.add(cb.and(attributeMatch, valueBetween, validPredicate));
            }
        }

        // Filtrage par population (FOLLOWED / UNFOLLOWED / ALL) via sous-requête sur
        // UserSubscriptionEntity
        FollowFilter scope = options.getPopulationScope();
        if (scope != null && scope != FollowFilter.ALL) {
            if (userId == null) {
                throw new IllegalArgumentException(
                        "userId est requis lorsque populationScope est FOLLOWED ou UNFOLLOWED");
            }

            // EXISTS (select 1 from UserSubscriptionEntity us
            // where us.id.personId = person.id and us.id.userId = :userId)
            Subquery<UserSubscriptionEntity> sub = cq.subquery(UserSubscriptionEntity.class);
            Root<UserSubscriptionEntity> us = sub.from(UserSubscriptionEntity.class);
            sub.select(us); // le contenu importe peu pour EXISTS

            Predicate personMatch = cb.equal(us.get("id").get("personId"), person.get("id"));
            Predicate userMatch = cb.equal(us.get("id").get("userId"), userId);
            sub.where(cb.and(personMatch, userMatch));

            if (scope == FollowFilter.FOLLOWED) {
                filterPredicates.add(cb.exists(sub));
            } else if (scope == FollowFilter.UNFOLLOWED) {
                filterPredicates.add(cb.not(cb.exists(sub)));
            }
        }

        if (!filterPredicates.isEmpty()) {
            cq.where(cb.and(filterPredicates.toArray(new Predicate[0])));
        }

        // DISTINCT pour éviter les doublons dus aux joins
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
        // Pour des chaînes plus longues, ajouter un caractère de fin max
        return value + "\uffff";
    }
}
