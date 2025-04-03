package com.saymyname.service;

import com.saymyname.core.model.challenge.ChallengeSeason;
import com.saymyname.persistence.dao.ChallengeSeasonDao;
import com.saymyname.persistence.entity.ChallengeSeasonEntity;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class ChallengeSeasonService {

    private final ChallengeSeasonDao challengeSeasonDao;

    public ChallengeSeasonService(ChallengeSeasonDao challengeSeasonDao) {
        this.challengeSeasonDao = challengeSeasonDao;
    }

    public Optional<ChallengeSeason> getCurrentSeason() {
        LocalDate today = LocalDate.now();
        // Recherche dans la base la saison qui couvre aujourd'hui
        return challengeSeasonDao.findSeasonCoveringDate(today);
    }

    /**
     * Crée la prochaine saison de challenge.
     * Exemple de logique : la prochaine saison commence une semaine après aujourd'hui.
     */
    public void createNextSeason() {
        System.out.println("Création automatique de la prochaine saison de challenge...");

        LocalDate today = LocalDate.now();
        // Définir la nouvelle saison : par exemple, démarrer le lundi suivant
        LocalDate nextStartDate = today.plusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate nextEndDate = nextStartDate.plusDays(6);

        // Détermination du numéro de saison
        // Si une saison couvrant aujourd'hui existe, incrémentez son numéro ; sinon, commencez à 1.
        Optional<ChallengeSeason> currentSeasonOpt = challengeSeasonDao.findSeasonCoveringDate(today);
        int newSeasonNumber = currentSeasonOpt.map(season -> season.getSeasonNumber() + 1).orElse(1);

        ChallengeSeasonEntity newSeason = new ChallengeSeasonEntity.Builder()
                .withSeasonNumber(newSeasonNumber)
                .withStartDate(nextStartDate)
                .withEndDate(nextEndDate)
                .build();

        challengeSeasonDao.save(newSeason);
        System.out.println("Nouvelle saison créée : " + newSeason);
    }

    /**
     * Vérifie si la saison de challenge couvrant la date d'aujourd'hui existe ;
     * si elle est manquante, la crée pour la période en cours (du lundi au dimanche).
     */
    public void createSeasonIfMissing() {
        System.out.println("Vérification de l'existence de la saison de challenge en cours...");
        LocalDate today = LocalDate.now();
        Optional<ChallengeSeason> currentSeason = challengeSeasonDao.findSeasonCoveringDate(today);
        if (!currentSeason.isPresent()) {
            // Définir la saison pour la semaine en cours : du lundi au dimanche
            LocalDate startDate = today.with(DayOfWeek.MONDAY);
            LocalDate endDate = startDate.plusDays(6);
            ChallengeSeasonEntity newSeason = new ChallengeSeasonEntity.Builder()
                    .withSeasonNumber(1) // Si aucune saison n'existe, on démarre à 1. Sinon, la logique peut être ajustée.
                    .withStartDate(startDate)
                    .withEndDate(endDate)
                    .build();
            challengeSeasonDao.save(newSeason);
            System.out.println("Saison manquante créée pour la période : " + startDate + " à " + endDate);
        } else {
            System.out.println("La saison de challenge en cours existe déjà : " + currentSeason.get());
        }
    }
}
