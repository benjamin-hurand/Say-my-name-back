package com.saymyname.service.course.scheduler;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.KnowledgeStatus;

/**
 * SM-2 spaced repetition scheduler with datetime precision.
 * Uses actual elapsed days for adjusting intervals.
 */
@Component
public class Sm2Scheduler implements SchedulerStrategy {

    private static final double MIN_EF = 1.3;
    private final Logger logger = LoggerFactory.getLogger(Sm2Scheduler.class);

    @Override
    public int mapGrade(boolean correct) {
        // Map to SM2 quality scale: 5 for perfect, 2 for failure
        return correct ? 5 : 2;
    }

    @Override
    public void schedule(Knowledge k, int q) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last = k.getLastReviewDate();

        // 1) Calculate actual elapsed days since last review (min 1 day)
        double elapsedDays = 1.0;
        if (last != null) {
            Duration duration = Duration.between(last, now);
            elapsedDays = Math.max(1.0, duration.toSeconds() / (24.0 * 3600.0));
        }

        // 2) Determine new repetition srsStreak and interval
        int srsStreak = k.getSrsStreak();
        double intervalDays;
        logger.info("quality = " + q);
        logger.info("srsStreak = " + srsStreak);
        if (q >= 3) {
            // Successful recall
            if (srsStreak <= 0) {
                intervalDays = 1.0;
            } else if (srsStreak == 1) {
                intervalDays = 6.0;
            } else {
                // Use actual elapsed time to adapt
                intervalDays = elapsedDays * k.getEaseFactor().doubleValue();
            }

            srsStreak = (srsStreak < 0) ? 1 : srsStreak + 1;
        } else {
            // Failure -> reset
            intervalDays = 1.0;
            srsStreak = (srsStreak > 0) ? -1 : srsStreak - 1;
        }
        logger.info("new srsStreak = " + srsStreak);
        // 3) Update ease factor
        double ef = k.getEaseFactor().doubleValue();
        ef += 0.1 - (5 - q) * (0.08 + (5 - q) * 0.02);
        ef = Math.max(MIN_EF, ef);

        // 4) Persist updated values
        k.setEaseFactor(BigDecimal.valueOf(ef));
        k.setSrsStreak(srsStreak);

        // 5) Update review timestamps
        k.setLastReviewDate(now);
        long nextDays = Math.round(intervalDays);
        k.setNextReviewDate(now.plusDays(nextDays));

        // 6) Update business status
        if (srsStreak >= 4) {
            k.setStatus(KnowledgeStatus.MASTERED);
        }
    }
}