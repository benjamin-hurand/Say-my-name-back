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

        interface AttributeMetaRow {
                Long getId();

                boolean getIdentitySource();

                int getDisplayOrder();

                Long getConceptId();

                String getConceptCode();

                Boolean getConceptDerived();

                String getPortabilityKind();

                Boolean getIdentityComponentEligible();
        }

        @Query("""
                        select a.id as id,
                               a.identitySource as identitySource,
                               a.displayOrder as displayOrder,
                               c.id as conceptId,
                               c.code as conceptCode,
                               c.derived as conceptDerived,
                               cast(c.portabilityKind as string) as portabilityKind,
                               c.identityComponentEligible as identityComponentEligible
                        from AttributeEntity a
                        left join a.concept c
                        where a.tenantId = :tenantId
                        """)
        List<AttributeMetaRow> findMetaByTenantId(@Param("tenantId") Long tenantId);
}