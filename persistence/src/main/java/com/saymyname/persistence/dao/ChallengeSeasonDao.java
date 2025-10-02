package com.saymyname.persistence.dao;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.saymyname.core.model.challenge.ChallengeSeason;
import com.saymyname.persistence.entity.organization.ChallengeSeasonEntity;
import com.saymyname.persistence.mapper.ChallengeSeasonEntityMapper;
import com.saymyname.persistence.repository.ChallengeSeasonRepository;

@Repository
public class ChallengeSeasonDao {

    private final ChallengeSeasonRepository seasonRepository;
    private final ChallengeSeasonEntityMapper challengeSeasonEntityMapper;

    public ChallengeSeasonDao(ChallengeSeasonRepository seasonRepository,
            ChallengeSeasonEntityMapper challengeSeasonEntityMapper) {
        this.seasonRepository = seasonRepository;
        this.challengeSeasonEntityMapper = challengeSeasonEntityMapper;
    }

    /**
     * Recherche une saison couvrant la date donnée.
     */
    public Optional<ChallengeSeason> findSeasonCoveringDate(LocalDateTime date) {
        return seasonRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date)
                .map(challengeSeasonEntityMapper::toModel);
    }

    /**
     * Sauvegarde une saison en convertissant le modèle en entité et vice versa.
     */
    public ChallengeSeason save(ChallengeSeason seasonModel) {
        ChallengeSeasonEntity entity = challengeSeasonEntityMapper.toEntity(seasonModel);
        ChallengeSeasonEntity savedEntity = seasonRepository.save(entity);
        return challengeSeasonEntityMapper.toModel(savedEntity);
    }
}
