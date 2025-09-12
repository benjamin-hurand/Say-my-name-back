package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.AttributeEntity;
import com.saymyname.persistence.projection.AttributeMinMaxProjection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttributeRepository extends JpaRepository<AttributeEntity, Long> {

    AttributeEntity findByAttributeName(String attributeName);

    @Query("SELECT a.id as id, " +
            "a.attributeName as attributeName, " +
            "a.maxValues as maxValues, " + // ← remplacé
            "a.filter as filter, " +
            "a.sort as sort, " +
            "a.initializable as initializable, " +
            "a.type as type, " +
            "MIN(CASE " +
            "  WHEN a.type = 'NUMBER' THEN CAST(pa.value AS integer) " +
            "  ELSE NULL " +
            "END) as minNumberValue, " +
            "MAX(CASE " +
            "  WHEN a.type = 'NUMBER' THEN CAST(pa.value AS integer) " +
            "  ELSE NULL " +
            "END) as maxNumberValue, " +
            "MIN(CASE " +
            "  WHEN a.type = 'DATE' THEN FUNCTION('STR_TO_DATE', pa.value, '%Y-%m-%d') " +
            "  ELSE NULL " +
            "END) as minDateValue, " +
            "MAX(CASE " +
            "  WHEN a.type = 'DATE' THEN FUNCTION('STR_TO_DATE', pa.value, '%Y-%m-%d') " +
            "  ELSE NULL " +
            "END) as maxDateValue " +
            "FROM AttributeEntity a " +
            "LEFT JOIN PersonAttributeEntity pa ON a.id = pa.attribute.id " +
            "WHERE a.filter = true " +
            "GROUP BY a.id, a.attributeName, a.maxValues, a.filter, a.sort, a.initializable, a.type")
    List<AttributeMinMaxProjection> findAttributesWithMinMax();

    List<AttributeEntity> findByFilterTrue();

    List<AttributeEntity> findBySortTrue();
}
