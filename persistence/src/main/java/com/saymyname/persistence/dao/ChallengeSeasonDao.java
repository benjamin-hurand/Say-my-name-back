package com.saymyname.persistence.dao;

import java.time.LocalDateTime;
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

    /**
     * Recherche la saison suivante, c'est-à-dire celle dont le numéro est égal à
     * (numéro de la saison actuelle + 1).
     */
    public Optional<ChallengeSeason> findNextSeason() {
        // On suppose ici que la saison actuelle existe.
        Optional<ChallengeSeason> currentSeasonOpt = findSeasonCoveringDate(LocalDateTime.now());
        if (currentSeasonOpt.isPresent()) {
            int nextSeasonNumber = currentSeasonOpt.get().getSeasonNumber() + 1;
            return seasonRepository.findBySeasonNumber(nextSeasonNumber)
                    .map(challengeSeasonEntityMapper::toModel);
        }
        return Optional.empty();
    }
}
