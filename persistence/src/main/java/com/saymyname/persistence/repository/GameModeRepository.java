package com.saymyname.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.organization.GameModeEntity;

@Repository
public interface GameModeRepository extends JpaRepository<GameModeEntity, Long> {

    @Query("""
                select gm
                from GameModeEntity gm
                where gm.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
                order by gm.id asc
            """)
    List<GameModeEntity> findAllInCurrentOrg();

    @Query("""
                select distinct gm
                from GameModeEntity gm
                left join fetch gm.gameModeAttributes gma
                left join fetch gma.attribute a
                where gm.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
                  and gm.id = :id
            """)
    Optional<GameModeEntity> findByIdWithAttributesInCurrentOrg(@Param("id") Long id);

    @Query("""
                select gm
                from GameModeEntity gm
                where gm.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
                  and gm.gameModeTitle = :title
            """)
    Optional<GameModeEntity> findByTitleInCurrentOrg(@Param("title") String title);

    @Modifying
    @Query("""
                delete from GameModeEntity gm
                where gm.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
                  and gm.id = :id
            """)
    int deleteInCurrentOrg(@Param("id") Long id);
}
