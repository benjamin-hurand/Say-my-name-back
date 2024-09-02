package com.oxyl.persistence.repository;

import com.oxyl.core.model.game.options.GameOptions;
import com.oxyl.core.model.game.options.GameAttributeFilter;
import com.oxyl.persistence.entity.AttributeEntity;
import com.oxyl.persistence.entity.PhotoEntity;
import com.oxyl.persistence.entity.PersonEntity;
import com.oxyl.persistence.entity.PersonAttributeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PhotoRepositoryCustomImpl implements PhotoRepositoryCustom {

    private final EntityManager entityManager;

    public PhotoRepositoryCustomImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Long> findPhotoIdsByDynamicFilters(GameOptions gameOptions) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<PhotoEntity> photo = query.from(PhotoEntity.class);
        Join<PhotoEntity, PersonEntity> person = photo.join("person");
        Join<PersonEntity, PersonAttributeEntity> attributes = person.join("attributes");
        Join<PersonAttributeEntity, AttributeEntity> attribute = attributes.join("attribute");

        List<Predicate> predicates = new ArrayList<>();

        for (GameAttributeFilter filter : gameOptions.getFilters()) {
            Predicate predicate = cb.and(
                    cb.equal(attribute.get("attributeName"), filter.getAttribute().getName()),
                    cb.between(attributes.get("value"), filter.getMinValue(), filter.getMaxValue())
            );
            predicates.add(predicate);
        }

        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        query.select(photo.get("id")).distinct(true);  // Mark query as distinct

        return entityManager.createQuery(query).getResultList();
    }

}
