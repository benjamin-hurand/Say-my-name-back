package com.saymyname.service.course.scheduler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.KnowledgeStatus;

@Component
public class PfaScheduler implements SchedulerStrategy {

    private static final double MIN_EF = 1.3;

    @Override
    public int mapGrade(boolean correct) {
        // utiliser 1 pour succès, 0 pour échec
        return correct ? 1 : 0;
    }

    @Override
    public void schedule(Knowledge k, int grade) {
        LocalDateTime today = LocalDateTime.now();

        // 0) Gérer le srsStreak commun à tous les algos ---
        int srsStreak = k.getSrsStreak();
        if (grade > 0) {
            // Si on passe d’une série d’échecs (srsStreak<0) à un succès,
            // on repart à 1, sinon on incrémente.
            srsStreak = (srsStreak < 0) ? 1 : srsStreak + 1;
        } else {
            // Si on passe d’une série de succès (srsStreak>0) à un échec,
            // on démarre à -1, sinon on décrémente.
            srsStreak = (srsStreak > 0) ? -1 : srsStreak - 1;
        }
        k.setSrsStreak(srsStreak);

        // 1) Calculer la "difficulty" via les compteurs déjà mis à jour
        double ratio = (k.getSuccessCount() + 1.0) / (k.getFailureCount() + 1.0);
        double ef = Math.max(Math.log(ratio), MIN_EF);
        k.setEaseFactor(BigDecimal.valueOf(ef));

        // 2) Calculer l'ancien intervalle
        LocalDateTime last = k.getLastReviewDate();
        LocalDateTime next = k.getNextReviewDate();
        long prevInterval = (last != null && next != null)
                ? Math.max(1, ChronoUnit.DAYS.between(last, next))
                : 1L;

        // 3) Déterminer le nouvel intervalle
        int interval = (grade > 0)
                ? (int) Math.round(prevInterval * ef)
                : 1;

        // 4) Mettre à jour les dates
        k.setLastReviewDate(today);
        k.setNextReviewDate(today.plusDays(interval));

        // 5) Mettre à jour le statut métier
        double successRate = k.getSuccessCount() / k.getTotalRepetitionCount();
        if (successRate >= 0.9 && k.getSuccessCount() >= 3) {
            k.setStatus(KnowledgeStatus.MASTERED);
        }
    }
}
