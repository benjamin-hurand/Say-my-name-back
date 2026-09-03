package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.organization.attribute.AttributeEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeRepository extends JpaRepository<AttributeEntity, Long> {

        @EntityGraph(attributePaths = "concept")
        @Query("""
                        select a
                        from AttributeEntity a
                        where a.attributeName = :attributeName
                        """)
        Optional<AttributeEntity> findByAttributeNameWithConcept(@Param("attributeName") String attributeName);

        @EntityGraph(attributePaths = "concept")
        @Query("""
                        select a
                        from AttributeEntity a
                        """)
        List<AttributeEntity> findAllWithConcept();

        @EntityGraph(attributePaths = "concept")
        @Query("""
                        select a
                        from AttributeEntity a
                        where a.id = :id
                        """)
        Optional<AttributeEntity> findByIdWithConcept(@Param("id") Long id);

        @EntityGraph(attributePaths = "concept")
        @Query("""
                        select a
                        from AttributeEntity a
                        where a.filter = true
                        """)
        List<AttributeEntity> findByFilterTrueWithConcept();

        @EntityGraph(attributePaths = "concept")
        @Query("""
                        select a
                        from AttributeEntity a
                        where a.sort = true
                        """)
        List<AttributeEntity> findBySortTrueWithConcept();

        @EntityGraph(attributePaths = "concept")
        @Query("""
                        select a
                        from AttributeEntity a
                        where a.id in :ids
                        """)
        List<AttributeEntity> findAllByIdInWithConcept(@Param("ids") List<Long> ids);

        @EntityGraph(attributePaths = "concept")
        @Query("""
                        select a
                        from AttributeEntity a
                        where a.tenantId = :tenantId
                          and a.id in :ids
                        """)
        List<AttributeEntity> findAllByTenantIdAndIdInWithConcept(
                        @Param("tenantId") Long tenantId,
                        @Param("ids") List<Long> ids);

        @Query("""
                        select (count(a) > 0)
                        from AttributeEntity a
                        where a.tenantId = :tenantId
                          and a.concept.id = :conceptId
                          and (:attributeId is null or a.id <> :attributeId)
                        """)
        boolean existsOtherByTenantIdAndConceptId(
                        @Param("tenantId") Long tenantId,
                        @Param("conceptId") Long conceptId,
                        @Param("attributeId") Long attributeId);

        @Query("""
                        select (count(a) > 0)
                        from AttributeEntity a
                        where a.tenantId = :tenantId
                          and a.attributeName = :attributeName
                          and (:attributeId is null or a.id <> :attributeId)
                        """)
        boolean existsOtherByTenantIdAndAttributeName(
                        @Param("tenantId") Long tenantId,
                        @Param("attributeName") String attributeName,
                        @Param("attributeId") Long attributeId);

        @Query(value = """
                        select count(*)
                        from attributes
                        where tenant_id = :tenantId
                          and concept_id = :conceptId
                        """, nativeQuery = true)
        long countByTenantIdAndConceptId(
                        @Param("tenantId") Long tenantId,
                        @Param("conceptId") Long conceptId);

        interface AttributeMetaRow {
                Long getId();

                boolean getIdentitySource();

                int getDisplayOrder();

                String getConceptCode();

        }

        @Query("""
                        select a.id as id,
                               a.identitySource as identitySource,
                               a.displayOrder as displayOrder,
                               c.code as conceptCode
                        from AttributeEntity a
                        left join a.concept c
                        where a.tenantId = :tenantId
                        """)
        List<AttributeMetaRow> findMetaByTenantId(@Param("tenantId") Long tenantId);
}
