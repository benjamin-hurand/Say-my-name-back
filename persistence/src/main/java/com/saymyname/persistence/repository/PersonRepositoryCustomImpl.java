package com.saymyname.persistence.repository;

import com.saymyname.core.model.game.options.GameAttributeFilter;
import com.saymyname.core.model.game.options.GameAttributeSort;
import com.saymyname.core.model.game.options.GameOptions;
import com.saymyname.persistence.entity.PersonEntity;
import com.saymyname.persistence.entity.PersonAttributeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

        // Préparer la liste des prédicats pour le filtrage
        List<Predicate> filterPredicates = new ArrayList<>();
        if (options.getFilters() != null) {
            for (GameAttributeFilter filter : options.getFilters()) {
                // Créer un join INNER pour s'assurer que la personne possède bien cet attribut
                Join<PersonEntity, PersonAttributeEntity> filterJoin = person.join("attributes", JoinType.INNER);
                // Condition : l'attribut doit correspondre (par son ID)
                Predicate attributeMatch = cb.equal(
                        filterJoin.get("attribute").get("id"),
                        filter.getAttribute().getId()
                );
                // Condition : la valeur est comprise entre minValue et maxValue (comparaison lexicographique)
                Predicate valueBetween = cb.between(
                        filterJoin.get("value"),
                        filter.getMinValue(),
                        filter.getMaxValue()
                );
                filterPredicates.add(cb.and(attributeMatch, valueBetween));
            }
        }
        if (!filterPredicates.isEmpty()) {
            cq.where(cb.and(filterPredicates.toArray(new Predicate[0])));
        }

        // On utilise DISTINCT pour éviter les doublons dus aux joins multiples
        cq.select(person).distinct(true);

        return entityManager.createQuery(cq).getResultList();
    }
}
