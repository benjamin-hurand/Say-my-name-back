package com.saymyname.service.course.scheduler;

import com.saymyname.core.model.course.Knowledge;

public interface SchedulerStrategy {
    /**
     * Convertit la justesse de la réponse en “grade” pour l’algorithme.
     * Par exemple : SM-2 peut prendre 0…5 ; FSRS 1…4.
     */
    int mapGrade(boolean correct);

    /**
     * Met à jour le modèle Knowledge en fonction du grade et règle
     * nextReviewDate, easeFactor, réussite/échec, stability, etc.
     */
    void schedule(Knowledge k, int grade);
}
