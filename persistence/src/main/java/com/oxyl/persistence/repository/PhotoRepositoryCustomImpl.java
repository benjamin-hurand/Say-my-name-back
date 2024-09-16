package com.oxyl.persistence.repository;

import com.oxyl.core.model.game.options.GameAttributeSort;
import com.oxyl.core.model.game.options.GameOptions;
import com.oxyl.core.model.game.options.GameAttributeFilter;
import com.oxyl.persistence.entity.AttributeEntity;
import com.oxyl.persistence.entity.PhotoEntity;
import com.oxyl.persistence.entity.PersonEntity;
import com.oxyl.persistence.entity.PersonAttributeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PhotoRepositoryCustomImpl implements PhotoRepositoryCustom {

    private final EntityManager entityManager;
    private static final Logger logger = LoggerFactory.getLogger(PhotoRepositoryCustomImpl.class);

    public PhotoRepositoryCustomImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Long> findPhotoIdsByDynamicFilters(GameOptions gameOptions) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<PhotoEntity> photo = query.from(PhotoEntity.class);
        Join<PhotoEntity, PersonEntity> person = photo.join("person");

        List<Predicate> predicates = new ArrayList<>();

        // Adding filters dynamically
        for (GameAttributeFilter filter : gameOptions.getFilters()) {
            Join<PersonEntity, PersonAttributeEntity> personAttributes = person.join("attributes", JoinType.LEFT);
            Join<PersonAttributeEntity, AttributeEntity> attribute = personAttributes.join("attribute");

            // Ensure that attribute name matches
            Predicate predicate = cb.and(
                    cb.equal(attribute.get("attributeName"), filter.getAttribute().getName()),
                    cb.between(personAttributes.get("value"), filter.getMinValue(), filter.getMaxValue())
            );
            predicates.add(predicate);
        }

        // Apply predicates to the query
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // Sorting logic
        List<Order> orderList = new ArrayList<>();
        if (gameOptions.getSortBy() != null && !gameOptions.getSortBy().isEmpty()) {
            for (GameAttributeSort sort : gameOptions.getSortBy()) {
                Join<PersonEntity, PersonAttributeEntity> personAttributes = person.join("attributes", JoinType.LEFT);
                Join<PersonAttributeEntity, AttributeEntity> attribute = personAttributes.join("attribute");

                // Sort by attribute name and order direction
                if ("ASC".equalsIgnoreCase(sort.getOrder())) {
                    orderList.add(cb.asc(attribute.get("attributeName")));
                } else if ("DESC".equalsIgnoreCase(sort.getOrder())) {
                    orderList.add(cb.desc(attribute.get("attributeName")));
                }
            }
        }

        // Apply sorting to the query
        if (!orderList.isEmpty()) {
            query.orderBy(orderList);
        }

        // Select photo IDs
        query.select(photo.get("id"));

        // Execute the query
        TypedQuery<Long> typedQuery = entityManager.createQuery(query);

        // Logging the generated query
        logger.debug("Executing query with sorting: {}", typedQuery.unwrap(org.hibernate.query.Query.class).getQueryString());

        return typedQuery.getResultList();
    }

}
