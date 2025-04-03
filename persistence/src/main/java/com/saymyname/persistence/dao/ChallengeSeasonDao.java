package com.saymyname.persistence.dao;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.saymyname.core.model.challenge.ChallengeSeason;
import com.saymyname.persistence.entity.ChallengeSeasonEntity;
import com.saymyname.persistence.mapper.ChallengeSeasonEntityMapper;
import com.saymyname.persistence.repository.ChallengeSeasonRepository;

@Repository
public class ChallengeSeasonDao {

    private final ChallengeSeasonRepository seasonRepository;
    private final ChallengeSeasonEntityMapper challengeSeasonEntityMapper;

    public ChallengeSeasonDao(ChallengeSeasonRepository seasonRepository, ChallengeSeasonEntityMapper challengeSeasonEntityMapper) {
        this.seasonRepository = seasonRepository;
        this.challengeSeasonEntityMapper = challengeSeasonEntityMapper;
    }

    /**
     * Recherche une saison couvrant la date donnée.
     */
    public Optional<ChallengeSeason> findSeasonCoveringDate(LocalDate date) {
        return seasonRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date).map(challengeSeasonEntityMapper::toModel);
    }

    /**
     * Sauvegarde une saison.
     */
    public ChallengeSeasonEntity save(ChallengeSeasonEntity season) {
        return seasonRepository.save(season);
    }
}
