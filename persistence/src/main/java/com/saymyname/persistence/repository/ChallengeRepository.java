package com.saymyname.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.ChallengeEntity;

@Repository
public interface ChallengeRepository extends JpaRepository<ChallengeEntity, Long>, ChallengeRepositoryCustom {
    @Query("SELECT c FROM ChallengeEntity c WHERE c.gameMode.id = ?1 " +
       "AND c.filterAttribute.id = ?2 " +
       "AND TRIM(UPPER(c.minFilterValue)) = TRIM(UPPER(?3)) " +
       "AND TRIM(UPPER(c.maxFilterValue)) = TRIM(UPPER(?4))")
    Optional<ChallengeEntity> findByGameMode_IdAndFilterAttribute_IdAndMinFilterValueAndMaxFilterValue(
        Long gameModeId, Long filterId, String minFilterValue, String maxFilterValue);


}

