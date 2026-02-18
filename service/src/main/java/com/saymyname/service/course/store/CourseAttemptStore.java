// src/main/java/com/saymyname/service/course/store/CourseAttemptStore.java
package com.saymyname.service.course.store;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionAttempt;
import com.saymyname.core.model.people.Fact;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.core.model.course.RecentAnswerStat;
import com.saymyname.service.quiz.store.QuizAttemptStore;

/**
 * Contrat "course-aware" : façade persistence pour les attempts COURSE en DB.
 *
 * Objectifs:
 * - sortir CourseService du détail DAO/service
 * - centraliser user-scoping + opérations ciblées (step state, answer meta...)
 * - exposer le minimum nécessaire pour être utilisé comme backend de
 * QuizAttemptStore
 */
public interface CourseAttemptStore {

    // --- CRUD attempt course ---
    CourseQuestionAttempt create(CourseQuestionAttempt attempt);

    CourseQuestionAttempt findById(Long id);

    void deleteAllByCourse(Course course);

    // --- Updates ciblées ---
    void updateStepState(Long attemptId, QuizQuestionSnapshot snapShot, String rawSubmission,
            String normalizedAudit);

    void updateAnswerMetaAndItems(CourseQuestionAttempt courseQuestion);

    // --- Help ---
    List<Fact> markHelpAndGetAttributes(Long courseId, Long questionId);

    void markHelpUsed(Long id);

    // --- Stats activité ---
    int countAllAnswersByCourse(Course course);

    int countAnswersSince(Course course, LocalDateTime since);

    LocalDateTime findLastAnsweredAt(Course course);

    List<RecentAnswerStat> findRecentAnswerStats(Course course, int limit);

    // ------------------------------------------------------------------
    // Bridge minimal pour l'unification avec QuizAttemptStore (COURSE)
    // ------------------------------------------------------------------

    /**
     * Lecture attempt course pour le flow unifié (multi-step).
     * Doit respecter le user-scoping.
     */
    Optional<QuizAttemptStore.StoredAttempt> peekAttempt(Long attemptId, Long userId);

    /**
     * COURSE: consume == peek (on ne supprime pas l'attempt en DB).
     */
    default Optional<QuizAttemptStore.StoredAttempt> consumeAttempt(Long attemptId, Long userId) {
        return peekAttempt(attemptId, userId);
    }

    /**
     * Mise à jour snapshot (multi-step) pour course.
     * Doit respecter le user-scoping.
     */
    boolean updateSnapshot(Long attemptId, Long userId, QuizQuestionSnapshot updatedSnapshot);

    /**
     * Multi-step update + audit (raw/normalized) — méthode figée.
     */
    boolean updateAttempt(Long attemptId, Long userId, QuizQuestionSnapshot updatedSnapshot,
            String rawSubmission, String normalizedAudit);
}
