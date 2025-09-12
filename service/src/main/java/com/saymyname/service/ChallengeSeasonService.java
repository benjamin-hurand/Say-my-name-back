// src/main/java/com/saymyname/service/ChallengeSeasonService.java
package com.saymyname.service;

import com.saymyname.core.model.challenge.ChallengeSeason;
import com.saymyname.core.model.challenge.SeasonConstants;
import com.saymyname.persistence.dao.ChallengeSeasonDao;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
public class ChallengeSeasonService {

    private final ChallengeSeasonDao challengeSeasonDao;

    // Cache en mémoire (instance unique Spring)
    private volatile ChallengeSeason cachedCurrentSeason;
    private volatile ChallengeSeason cachedNextSeason;

    public ChallengeSeasonService(ChallengeSeasonDao challengeSeasonDao) {
        this.challengeSeasonDao = challengeSeasonDao;
    }

    /* ===================== API publique minimale ===================== */

    /** Version Optional — à privilégier côté appelant. */
    public Optional<ChallengeSeason> getCurrentSeasonOpt() {
        return getSeasonOptInternal(SeasonKind.CURRENT);
    }

    /** Version Optional — à privilégier côté appelant. */
    public Optional<ChallengeSeason> getNextSeasonOpt() {
        return getSeasonOptInternal(SeasonKind.NEXT);
    }

    /** Variante qui jette si indisponible (s’appuie sur la version Optional). */
    public ChallengeSeason getCurrentSeasonOrThrow() {
        return getCurrentSeasonOpt()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Invariant season: aucune saison disponible"));
    }

    /** Variante qui jette si indisponible (s’appuie sur la version Optional). */
    public ChallengeSeason getNextSeasonOrThrow() {
        return getNextSeasonOpt()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Invariant season: aucune saison disponible"));
    }

    /* ===================== Hooks démarrage + cron ===================== */

    /** Démarrage : assure les saisons en BD et chauffe le cache (1 seule passe). */
    public void onAppStart() {
        ensureAndWarmNow();
        // Purge déplacée dans SeasonMaintenanceService pour éviter les dépendances
        // croisées
    }

    /** Tick planifié (ex. lundi 09:00) : même logique que le démarrage. */
    public void onCronTick() {
        ensureAndWarmNow();
        // Purge déplacée dans SeasonMaintenanceService pour éviter les dépendances
        // croisées
    }

    /** Expose aussi une action manuelle si tu veux déclencher depuis ailleurs. */
    public boolean ensureAndWarmNow() {
        SeasonsPair pair = ensureSeasonsFor(LocalDateTime.now());
        return warmCacheAndReturnIfChanged(pair);
    }

    /* ===================== Implémentation factorisée ===================== */

    private enum SeasonKind {
        CURRENT, NEXT
    }

    /**
     * Chemin principal sans exceptions : tente le cache, sinon assure en BD et warm
     * le cache.
     */
    private Optional<ChallengeSeason> getSeasonOptInternal(SeasonKind kind) {
        LocalDateTime now = LocalDateTime.now();

        // 1) Cache valide ? (et next présent si demandé)
        if (!isCurrentCacheValid(now) || (kind == SeasonKind.NEXT && cachedNextSeason == null)) {
            // 2) Cache invalide → assure en BD (trouve ou crée) puis warm le cache
            SeasonsPair pair = ensureSeasonsFor(now);
            warmCacheAndReturnIfChanged(pair);
        }

        ChallengeSeason result = (kind == SeasonKind.CURRENT) ? cachedCurrentSeason : cachedNextSeason;
        return Optional.ofNullable(result);
    }

    /** Cache valide si la "current" existe et couvre 'now'. */
    private boolean isCurrentCacheValid(LocalDateTime now) {
        ChallengeSeason c = this.cachedCurrentSeason;
        return c != null && !now.isBefore(c.getStartDate()) && !now.isAfter(c.getEndDate());
    }

    /**
     * Assure la présence de la saison courante (couvrant 'ref') et de la suivante :
     * - tente de trouver en BD
     * - crée si manquante
     * Retourne les 2 objets (créés ou trouvés) pour warm le cache sans re-SELECT.
     */
    private SeasonsPair ensureSeasonsFor(LocalDateTime ref) {
        // 1) Saison courante
        ChallengeSeason current = challengeSeasonDao.findSeasonCoveringDate(ref).orElse(null);
        if (current == null) {
            LocalDateTime start = seasonStartFor(ref);
            LocalDateTime end = start.plusDays(SeasonConstants.DURATION_DAYS).minusSeconds(1);
            int seasonNumber = 1; // par défaut si table vide (ajuste via DAO si besoin)
            current = challengeSeasonDao.save(new ChallengeSeason.Builder()
                    .withSeasonNumber(seasonNumber)
                    .withStartDate(start)
                    .withEndDate(end)
                    .build());
        }

        // 2) Saison suivante (démarre à +1s de la fin de l’actuelle)
        LocalDateTime nextStart = current.getEndDate().plusSeconds(1);
        ChallengeSeason next = challengeSeasonDao.findSeasonCoveringDate(nextStart).orElse(null);
        if (next == null) {
            LocalDateTime nextEnd = nextStart.plusDays(SeasonConstants.DURATION_DAYS).minusSeconds(1);
            int nextNumber = current.getSeasonNumber() + 1;
            next = challengeSeasonDao.save(new ChallengeSeason.Builder()
                    .withSeasonNumber(nextNumber)
                    .withStartDate(nextStart)
                    .withEndDate(nextEnd)
                    .build());
        }

        return new SeasonsPair(current, next);
    }

    /** Écrit les deux saisons dans le cache (idempotent). */
    private void warmCache(SeasonsPair pair) {
        if (!sameSeason(this.cachedCurrentSeason, pair.current)) {
            this.cachedCurrentSeason = pair.current;
        }
        if (!sameSeason(this.cachedNextSeason, pair.next)) {
            this.cachedNextSeason = pair.next;
        }
    }

    /** Warm + renvoie si la "current" a changé (utile pour déclencher des jobs). */
    private boolean warmCacheAndReturnIfChanged(SeasonsPair pair) {
        boolean changed = !sameSeason(this.cachedCurrentSeason, pair.current);
        warmCache(pair);
        return changed;
    }

    private boolean sameSeason(ChallengeSeason a, ChallengeSeason b) {
        if (a == null || b == null)
            return a == b;
        // Compare saison par (numéro + bornes) — adapte si tu as un ID stable
        return a.getSeasonNumber() == b.getSeasonNumber()
                && Objects.equals(a.getStartDate(), b.getStartDate())
                && Objects.equals(a.getEndDate(), b.getEndDate());
    }

    /** Calcule le début de saison à partir des constantes (jour + heure). */
    private LocalDateTime seasonStartFor(LocalDateTime ref) {
        return ref.with(SeasonConstants.START_DOW).with(SeasonConstants.START_TIME);
    }

    /* ===================== Types internes ===================== */
    private record SeasonsPair(ChallengeSeason current, ChallengeSeason next) {
    }
}
