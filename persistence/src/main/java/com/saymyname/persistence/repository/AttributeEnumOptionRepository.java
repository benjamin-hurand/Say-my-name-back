package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.organization.attribute.AttributeEnumOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface AttributeEnumOptionRepository extends JpaRepository<AttributeEnumOptionEntity, Long> {

        @Query("select o.code from AttributeEnumOptionEntity o " +
                        "where o.attribute.id = :attributeId and o.active = true")
        Set<String> findActiveCodesByAttributeId(@Param("attributeId") Long attributeId);

        @Query("select o.code from AttributeEnumOptionEntity o " +
                        "where o.attribute.id = :attributeId")
        Set<String> findAllCodesByAttributeId(@Param("attributeId") Long attributeId);

        // utile pour l'UI (liste d’options actives ordonnées)
        List<AttributeEnumOptionEntity> findByAttribute_IdAndActiveTrueOrderByOrderIndexAscLabelAsc(Long attributeId);

        List<AttributeEnumOptionEntity> findByAttribute_IdInAndActiveTrueOrderByAttribute_IdAscOrderIndexAscLabelAsc(
                        Collection<Long> attributeIds);
}
