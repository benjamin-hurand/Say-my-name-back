package com.saymyname.persistence.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.enums.FollowFilter;
import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.core.model.quiz.options.CategorySelection;
import com.saymyname.core.model.quiz.options.TrainingOptions;
import com.saymyname.persistence.entity.organization.FactEntity;
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
    public List<PersonEntity> findByOptions(TrainingOptions options, Long userId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<PersonEntity> cq = cb.createQuery(PersonEntity.class);
        Root<PersonEntity> person = cq.from(PersonEntity.class);

        // Récupérer la photo APPROVED
        person.fetch("photos", JoinType.INNER);
        Join<PersonEntity, PhotoEntity> photoJoin = person.join("photos", JoinType.INNER);

        // Liste des prédicats de filtrage
        List<Predicate> filterPredicates = new ArrayList<>();
        filterPredicates.add(cb.equal(photoJoin.get("status"), PhotoStatus.APPROVED));

        // Filtrage par catégorie (single category selection)
        CategorySelection category = options.getCategory();
        if (category != null && category.getAttributeId() != null) {
            // Join INNER sur les attributs
            Join<PersonEntity, FactEntity> attrJoin = person.join("attributes", JoinType.INNER);

            // Attribut ciblé (par ID)
            Predicate attributeMatch = cb.equal(
                    attrJoin.get("attribute").get("id"),
                    category.getAttributeId());

            // Valeur exacte
            Predicate valueMatch = cb.equal(attrJoin.get("value"), category.getValue());

            // Validité temporelle de l'attribut
            Predicate validFromPredicate = cb.lessThanOrEqualTo(attrJoin.get("validFrom"), cb.currentTimestamp());
            Predicate validToPredicate = cb.or(
                    cb.isNull(attrJoin.get("validTo")),
                    cb.greaterThanOrEqualTo(attrJoin.get("validTo"), cb.currentTimestamp()));
            Predicate notPendingDelete = cb.isFalse(attrJoin.get("pendingDelete"));
            Predicate validPredicate = cb.and(validFromPredicate, validToPredicate, notPendingDelete);

            // Combinaison pour ce filtre
            filterPredicates.add(cb.and(attributeMatch, valueMatch, validPredicate));
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
}
