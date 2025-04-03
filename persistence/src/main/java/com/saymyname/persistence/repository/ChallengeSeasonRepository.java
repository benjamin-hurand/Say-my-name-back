package com.saymyname.persistence.repository;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.saymyname.persistence.entity.ChallengeSeasonEntity;

@Repository
public interface ChallengeSeasonRepository extends JpaRepository<ChallengeSeasonEntity, Long> {
    
    /**
     * Retourne la saison qui couvre la date spécifiée.
     * On suppose que start_date <= date et end_date >= date.
     */
    Optional<ChallengeSeasonEntity> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate date1, LocalDate date2);
}
