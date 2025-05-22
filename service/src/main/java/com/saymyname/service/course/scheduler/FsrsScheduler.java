package com.saymyname.service.course.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.KnowledgeStatus;

/**
 * FSRS (Forgetting Spaced Repetition Scheduler) implementation
 * with time-based decay and configurable weights.
 */
@Component
public class FsrsScheduler implements SchedulerStrategy {
    // FSRS parameters (configurable via application.properties)
    @Value("${fsrs.w1:0.17}")
    private double W1;
    @Value("${fsrs.w2:0.0}")
    private double W2;
    @Value("${fsrs.w3:0.01}")
    private double W3;
    @Value("${fsrs.w4:1.0}")
    private double W4;
    @Value("${fsrs.w5:0.2}")
    private double W5;
    @Value("${fsrs.w6:1.3}")
    private double W6;
    @Value("${fsrs.targetRetention:0.9}")
    private double TARGET_RETENTION;

    @Override
    public int mapGrade(boolean correct) {
        // Map to FSRS 0-5 scale: no help correct=5, with help=3, incorrect=0
        return correct ? 5 : 0;
    }

    @Override
    public void schedule(Knowledge k, int G) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last = k.getLastReviewDate();

        // 0) Gérer le srsStreak commun à tous les algos ---
        int srsStreak = k.getSrsStreak();
        if (G > 0) {
            // Si on passe d’une série d’échecs (srsStreak<0) à un succès,
            // on repart à 1, sinon on incrémente.
            srsStreak = (srsStreak < 0) ? 1 : srsStreak + 1;
        } else {
            // Si on passe d’une série de succès (srsStreak>0) à un échec,
            // on démarre à -1, sinon on décrémente.
            srsStreak = (srsStreak > 0) ? -1 : srsStreak - 1;
        }
        k.setSrsStreak(srsStreak);

        // 1) Compute elapsed time in days (as double)
        double t = 0;
        if (last != null) {
            Duration delta = Duration.between(last, now);
            t = delta.toSeconds() / (24.0 * 3600.0);
        }

        // 2) Retrieve previous stability (M) and difficulty (D)
        double M = k.getStability();
        double D = k.getDifficulty();

        // 3) Predict retention based on forgetting curve
        // R_pred = exp(- (t^W1) / (M^W2) )
        double R_pred = Math.exp(-Math.pow(t, W1) / Math.pow(Math.max(M, 1e-6), W2));

        // 4) Update retention estimate (R_new)
        // R_new = TARGET_RETENTION + W3*(G - R_pred) + W4
        double R_new = TARGET_RETENTION + W3 * (G - R_pred) + W4;

        // 5) Update difficulty (D_new)
        // D_new = D * (1 + W5 * (R_new - TARGET_RETENTION))^W6
        double D_new = D * Math.pow(1 + W5 * (R_new - TARGET_RETENTION), W6);
        k.setDifficulty(D_new);

        // 6) Update stability (M_new)
        // M_new = (t > 0) ? (t / Math.log(R_pred > 0 ? R_pred : 1e-6)) : M
        double M_new;
        if (t > 0 && R_new > 0) {
            M_new = Math.max(1.0, t / -Math.log(R_new));
        } else {
            M_new = Math.max(1.0, M);
        }
        k.setStability(M_new);

        // 7) Compute next interval in days = round(M_new)
        int interval = (int) Math.max(1, Math.round(M_new));
        k.setNextReviewDate(now.plusDays(interval));

        // 8) Update last review timestamp
        k.setLastReviewDate(now);

        // 9) Business status based on stability
        if (M_new > 30 && k.getSrsStreak() >= 4) {
            k.setStatus(KnowledgeStatus.MASTERED);
        }
    }
}
