// src/main/java/com/saymyname/webapp/dto/course/CourseStatsDto.java
package com.saymyname.webapp.dto.course;

import java.time.LocalDateTime;

import com.saymyname.core.model.enums.PopulationScope;

public record CourseStatsDto(
                // Meta
                Long courseId,
                Long userId,
                Long gameModeId,
                PopulationScope populationScope,

                // Répartition des knowledges (créés)
                int unknown,
                int discovered,
                int learned,
                int mastered,
                int createdTotal,

                // Candidats & univers
                long totalCandidates, // suivis si FOLLOWED, sinon total persons
                long universeEligible, // total persons global

                // Dérivés UI
                int knownTotal, // = createdTotal (alias UI)
                int remainingUnseen, // = max(0, totalCandidates - createdTotal)
                int progressPercent, // = round(100 * mastered / max(1, createdTotal))
                double createdCoverageRatio, // = createdTotal / max(1, totalCandidates)
                double masteredRatio, // = mastered / max(1, createdTotal)
                boolean finished, // = createdTotal > 0 && mastered == createdTotal

                // Activité
                int totalAnswers,
                int answersToday,
                LocalDateTime lastActivity,
                int currentRound,

                // Optionnel (si tu ajoutes un compteur SRS dues)
                Integer dueNow, // nullable
                int canGrowBy // = max(0, universeEligible - totalCandidates)
) {
}
