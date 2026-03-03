// src/main/java/com/saymyname/persistence/repository/AttributeRepository.java
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

    // --- projection minimale pour le cache (par tenant) ---
    interface AttributeMetaRow {
        Long getId();

        boolean getIdentitySource(); // alias property = identitySource

        boolean getDerived(); // alias property = derived

        int getDisplayOrder();
    }

    @Query("""
              select a.id as id,
                     a.identitySource as identitySource,
                     a.derived as derived,
                     a.displayOrder as displayOrder
              from AttributeEntity a
            """)
    List<AttributeMetaRow> findMetaForCurrentTenant();

    @Query("""
              select a
              from AttributeEntity a
              where a.id in :ids
            """)
    List<AttributeEntity> findAllByIdIn(@Param("ids") List<Long> ids);
}