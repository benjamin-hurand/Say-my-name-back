package com.saymyname.service;

import com.saymyname.core.model.challenge.ChallengeSeason;
import com.saymyname.persistence.dao.ChallengeSeasonDao;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
public class ChallengeSeasonService {

    private final ChallengeSeasonDao challengeSeasonDao;

    public ChallengeSeasonService(ChallengeSeasonDao challengeSeasonDao) {
        this.challengeSeasonDao = challengeSeasonDao;
    }

    /**
     * Renvoie la saison de challenge couvrant le moment donné.
     * 
     * @param now la date et heure actuelle
     * @return Optional contenant la saison si trouvée, sinon vide.
     */
    public Optional<ChallengeSeason> getCurrentSeason(LocalDateTime now) {
        return challengeSeasonDao.findSeasonCoveringDate(now);
    }

    /**
     * Renvoie la saison suivante (celle dont le numéro est N+1 par rapport à la saison actuelle).
     */
    public Optional<ChallengeSeason> getNextSeason() {
        return challengeSeasonDao.findNextSeason();
    }

    /**
     * Vérifie et crée la saison en cours et la saison suivante (N+1).
     */
    public void createSeasonsIfMissing() {
        createCurrentSeasonIfMissing();
        createNextSeasonIfMissing();
    }

    /**
     * Crée la saison couvrant le moment actuel, si elle est manquante.
     * La saison commence le lundi à 9h et se termine le dimanche à 23h59m59s.
     */
    public void createCurrentSeasonIfMissing() {
        LocalDateTime now = LocalDateTime.now();
        // Calculer le lundi de la semaine en cours à 9h
        LocalDateTime startDate = now.with(DayOfWeek.MONDAY).with(LocalTime.of(9, 0));
        // Fin : exactement 7 jours plus tard, moins 1 seconde (donc la saison dure exactement 7 jours)
        LocalDateTime endDate = startDate.plusDays(7).minusSeconds(1);
        Optional<ChallengeSeason> currentSeasonOpt = challengeSeasonDao.findSeasonCoveringDate(now);
        if (!currentSeasonOpt.isPresent()) {
            // Créer la saison pour la semaine en cours
            ChallengeSeason newSeason = new ChallengeSeason.Builder()
                    .withSeasonNumber(1)  // Démarrage à 1 si aucune saison n'existe
                    .withStartDate(startDate)
                    .withEndDate(endDate)
                    .build();
            ChallengeSeason savedSeason = challengeSeasonDao.save(newSeason);
            System.out.println("Saison en cours créée : " + savedSeason.getStartDate() + " à " + savedSeason.getEndDate());
        } else {
            System.out.println("Saison en cours déjà existante : " + currentSeasonOpt.get());
        }
    }
    

    /**
     * Crée la saison suivante (N+1) si elle n'existe pas.
     * Cette méthode utilise getNextSeason() pour vérifier son existence.
     */
    public void createNextSeasonIfMissing() {
        // On tente d'obtenir la saison couvrant l'instant actuel.
        Optional<ChallengeSeason> currentSeasonOpt = challengeSeasonDao.findSeasonCoveringDate(LocalDateTime.now());
        // Définir la durée d'une saison en jours (ici 7 jours)
        final int SEASON_DURATION_DAYS = 7;
        LocalDateTime nextStartDate;
        if (currentSeasonOpt.isPresent()) {
            // La saison suivante commence 1 seconde après la fin de la saison courante.
            nextStartDate = currentSeasonOpt.get().getEndDate().plusSeconds(1);
        } else {
            // Si aucune saison n'existe, on part du prochain lundi à 9h.
            LocalDateTime now = LocalDateTime.now();
            nextStartDate = now.plusWeeks(1).with(DayOfWeek.MONDAY).with(LocalTime.of(9, 0));
        }
        
        // Vérifier si une saison couvrant nextStartDate existe déjà.
        Optional<ChallengeSeason> nextSeasonOpt = challengeSeasonDao.findSeasonCoveringDate(nextStartDate);
        if (nextSeasonOpt.isEmpty()) {
            // La fin de la saison est calculée en ajoutant SEASON_DURATION_DAYS et en retirant 1 seconde.
            LocalDateTime nextEndDate = nextStartDate.plusDays(SEASON_DURATION_DAYS).minusSeconds(1);
            int newSeasonNumber = currentSeasonOpt.map(season -> season.getSeasonNumber() + 1).orElse(1);
            ChallengeSeason newSeason = new ChallengeSeason.Builder()
                    .withSeasonNumber(newSeasonNumber)
                    .withStartDate(nextStartDate)
                    .withEndDate(nextEndDate)
                    .build();
            ChallengeSeason savedSeason = challengeSeasonDao.save(newSeason);
            System.out.println("Saison suivante créée : " + savedSeason.getStartDate() + " à " + savedSeason.getEndDate());
        } else {
            System.out.println("Saison suivante déjà existante : " + nextSeasonOpt.get());
        }
    }
}
