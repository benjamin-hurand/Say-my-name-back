package com.saymyname.persistence.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.saymyname.persistence.entity.ChallengeSeasonEntity;

@Repository
public interface ChallengeSeasonRepository extends JpaRepository<ChallengeSeasonEntity, Long> {
    
    // Recherche la saison couvrant une date donnée (en LocalDateTime)
    Optional<ChallengeSeasonEntity> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDateTime date1, LocalDateTime date2);

    // Méthode dédiée pour récupérer la saison avec une date de début exacte
    Optional<ChallengeSeasonEntity> findByStartDate(LocalDateTime startDate);

    Optional<ChallengeSeasonEntity> findBySeasonNumber(int seasonNumber);
}
