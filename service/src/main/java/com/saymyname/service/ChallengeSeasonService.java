// src/main/java/com/saymyname/service/ChallengeSeasonService.java
package com.saymyname.service;

import com.saymyname.core.model.challenge.ChallengeSeason;
import com.saymyname.core.model.challenge.SeasonConstants;
import com.saymyname.core.multitenancy.OrgContext;
import com.saymyname.persistence.dao.ChallengeSeasonDao;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ChallengeSeasonService {

    private final ChallengeSeasonDao challengeSeasonDao;

    // Cache en mémoire par organisation (clé = orgId)
    private final ConcurrentMap<Long, SeasonsPair> cache = new ConcurrentHashMap<>();

    public ChallengeSeasonService(ChallengeSeasonDao challengeSeasonDao) {
        this.challengeSeasonDao = challengeSeasonDao;
    }

    /* ===================== API publique minimale ===================== */

    /**
     * Version Optional — à privilégier côté appelant (doit avoir un OrgContext en
     * place).
     */
    public Optional<ChallengeSeason> getCurrentSeasonOpt() {
        return getSeasonOptInternal(SeasonKind.CURRENT);
    }

    /**
     * Version Optional — à privilégier côté appelant (doit avoir un OrgContext en
     * place).
     */
    public Optional<ChallengeSeason> getNextSeasonOpt() {
        return getSeasonOptInternal(SeasonKind.NEXT);
    }

    /** Variante qui jette si indisponible (doit avoir un OrgContext en place). */
    public ChallengeSeason getCurrentSeasonOrThrow() {
        return getCurrentSeasonOpt()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Invariant season: aucune saison disponible"));
    }

    /** Variante qui jette si indisponible (doit avoir un OrgContext en place). */
    public ChallengeSeason getNextSeasonOrThrow() {
        return getNextSeasonOpt()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Invariant season: aucune saison disponible"));
    }

    /*
     * ========= Helpers sûrs pour être appelés SANS contexte préexistant =========
     */

    /**
     * Initialise/rafraîchit pour UNE organisation (pose et restaure l’OrgContext).
     */
    public boolean ensureAndWarmNowForOrg(Long orgId) {
        try (AutoCloseable ctx = useOrg(orgId)) {
            return ensureAndWarmNow();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) { // AutoCloseable#close checked
            throw new IllegalStateException(e);
        }
    }

    /**
     * Lit la saison courante pour UNE org (convenience sans imposer un contexte
     * appelant).
     */
    public Optional<ChallengeSeason> getCurrentSeasonOptForOrg(Long orgId) {
        try (AutoCloseable ctx = useOrg(orgId)) {
            return getCurrentSeasonOpt();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Lit la prochaine saison pour UNE org (convenience sans imposer un contexte
     * appelant).
     */
    public Optional<ChallengeSeason> getNextSeasonOptForOrg(Long orgId) {
        try (AutoCloseable ctx = useOrg(orgId)) {
            return getNextSeasonOpt();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /*
     * ===================== Hooks (doivent être appelés avec contexte)
     * =====================
     */

    /**
     * Démarrage : assure les saisons en BD et chauffe le cache (1 passe). REQUIERT
     * OrgContext.
     */
    public void onAppStart() {
        ensureAndWarmNow();
    }

    /** Tick planifié (ex. lundi 09:00). REQUIERT OrgContext. */
    public void onCronTick() {
        ensureAndWarmNow();
    }

    /** Action interne : utilise l’OrgContext courant. */
    public boolean ensureAndWarmNow() {
        final Long orgId = requireOrgId();
        final SeasonsPair fresh = ensureSeasonsFor(LocalDateTime.now());
        return warmCacheAndReturnIfChanged(orgId, fresh);
    }

    /* ===================== Implémentation factorisée ===================== */

    private enum SeasonKind {
        CURRENT, NEXT
    }

    /**
     * Chemin principal : tente le cache pour l’org courante, sinon assure en BD
     * puis warm.
     */
    private Optional<ChallengeSeason> getSeasonOptInternal(SeasonKind kind) {
        final Long orgId = requireOrgId();
        final LocalDateTime now = LocalDateTime.now();

        SeasonsPair pair = cache.get(orgId);
        final boolean currentValid = isCurrentCacheValid(pair, now);
        final boolean needsNext = (kind == SeasonKind.NEXT);

        if (!currentValid || (needsNext && (pair == null || pair.next == null))) {
            pair = ensureSeasonsFor(now);
            warmCache(orgId, pair);
        }

        if (pair == null)
            return Optional.empty();
        return Optional.of(kind == SeasonKind.CURRENT ? pair.current : pair.next);
    }

    /** Cache valide si la "current" existe et couvre 'now'. */
    private boolean isCurrentCacheValid(SeasonsPair pair, LocalDateTime now) {
        if (pair == null || pair.current == null)
            return false;
        return !now.isBefore(pair.current.getStartDate()) && !now.isAfter(pair.current.getEndDate());
    }

    /**
     * Assure la présence de la saison courante (couvrant 'ref') et de la suivante.
     * Le DAO est tenant-aware via OrgContext/filters.
     */
    private SeasonsPair ensureSeasonsFor(LocalDateTime ref) {
        // 1) Saison courante
        ChallengeSeason current = challengeSeasonDao.findSeasonCoveringDate(ref).orElse(null);
        if (current == null) {
            LocalDateTime start = seasonStartFor(ref);
            LocalDateTime end = start.plusDays(SeasonConstants.DURATION_DAYS).minusSeconds(1);
            int seasonNumber = 1; // si table vide, sinon laisse le DAO calculer
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

    /** Écrit les deux saisons dans le cache (idempotent) pour l’org fournie. */
    private void warmCache(Long orgId, SeasonsPair pair) {
        cache.put(orgId, pair);
    }

    /** Warm + renvoie si la "current" a changé (utile pour déclencher des jobs). */
    private boolean warmCacheAndReturnIfChanged(Long orgId, SeasonsPair fresh) {
        SeasonsPair old = cache.put(orgId, fresh);
        return !sameSeason(old == null ? null : old.current, fresh.current);
    }

    private Long requireOrgId() {
        Long orgId = OrgContext.get();
        if (orgId == null) {
            throw new IllegalStateException("OrgContext manquant (organization_id requis).");
        }
        return orgId;
    }

    /** Utilitaire try-with-resources pour poser/restaurer l’OrgContext. */
    private AutoCloseable useOrg(Long orgId) {
        final Long previous = OrgContext.get();
        OrgContext.set(orgId);
        return () -> OrgContext.set(previous);
    }

    private boolean sameSeason(ChallengeSeason a, ChallengeSeason b) {
        if (a == null || b == null)
            return a == b;
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
