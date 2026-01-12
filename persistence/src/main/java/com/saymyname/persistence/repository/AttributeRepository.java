package com.saymyname.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.organization.attribute.AttributeEntity;

@Repository
public interface AttributeRepository extends JpaRepository<AttributeEntity, Long> {

    AttributeEntity findByAttributeName(String attributeName);

    List<AttributeEntity> findByFilterTrue();

    List<AttributeEntity> findBySortTrue();

    // --- NOUVEAU: projection minimale pour le cache (par org) ---
    interface AttributeMetaRow {
        Long getId();

        boolean getPrimaryField(); // alias = primaryField

        int getDisplayOrder(); // alias = displayOrder
    }

    @Query("""
            select a.id as id, a.primaryField as primaryField, a.displayOrder as displayOrder
            from AttributeEntity a
            where a.organizationId = :orgId
            """)
    List<AttributeMetaRow> findMetaByOrgId(@Param("orgId") Long orgId);

    @Query("""
                select a
                from AttributeEntity a
                where a.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
                  and a.id in :ids
            """)
    List<AttributeEntity> findAllByIdsInCurrentOrg(@Param("ids") List<Long> ids);
}
