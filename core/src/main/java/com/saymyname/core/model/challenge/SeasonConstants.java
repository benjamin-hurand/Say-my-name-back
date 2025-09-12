package com.saymyname.core.model.challenge;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Source de vérité pour les règles de saison (version "en dur").
 * Si un jour tu passes à une config DB/admin, tu pourras remplacer ce fichier
 * par
 * un service/config dynamique sans toucher au code appelant.
 */
public final class SeasonConstants {
    private SeasonConstants() {
    }

    /** Jour de démarrage de la saison. */
    public static final DayOfWeek START_DOW = DayOfWeek.MONDAY;

    /** Heure de démarrage de la saison (heure locale du serveur). */
    public static final LocalTime START_TIME = LocalTime.of(9, 0);

    /** Durée d'une saison (en jours). */
    public static final int DURATION_DAYS = 7;

    /**
     * Cron Spring du “passage de saison”.
     * Doit correspondre à START_DOW + START_TIME (ici: lundi 09:00).
     */
    public static final String BOUNDARY_CRON = "0 0 9 * * MON";
}
