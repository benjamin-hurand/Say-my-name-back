package com.saymyname.core.model.course;

import com.saymyname.core.model.enums.PoolType;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CourseQuestionAttempt {

    Long id;

    // Rich: on garde CourseId pour les mappers/services (Course complet optionnel
    // si tu veux plus tard)
    Long courseId;

    int questionRound;

    Instant askedAt;
    Instant answeredAt;
    int responseTimeMs;

    String rawSubmission;
    String normalizedAudit;

    boolean globalCorrect;

    PoolType poolType;
    boolean helpUsed;

    // ✅ Rich objects
    QuizQuestionSnapshot snapshot;
    CourseQuestionPlan plan;

    @Builder.Default
    List<CourseQuestionItem> items = List.of();

    public void validateInvariants() {
        if (courseId == null) {
            throw new IllegalStateException("CourseQuestionAttempt.courseId is required");
        }
        if (askedAt == null) {
            throw new IllegalStateException("CourseQuestionAttempt.askedAt is required");
        }
        if (poolType == null) {
            throw new IllegalStateException("CourseQuestionAttempt.poolType is required");
        }
        if (questionRound < 0) {
            throw new IllegalStateException("CourseQuestionAttempt.questionRound must be >= 0");
        }
        if (responseTimeMs < 0) {
            throw new IllegalStateException("CourseQuestionAttempt.responseTimeMs must be >= 0");
        }

        if (snapshot != null) {
            snapshot.validateInvariants();
        }
        if (plan != null) {
            plan.validateInvariants();
        }

        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("CourseQuestionAttempt must contain at least 1 item");
        }
        for (CourseQuestionItem item : items) {
            if (item == null)
                throw new IllegalStateException("CourseQuestionAttempt.items cannot contain null");
            item.validateInvariants();
        }
    }

    /**
     * Helpers “style domain” (sans muter l’objet : retourne une copie).
     */
    public CourseQuestionAttempt withAddedItem(CourseQuestionItem item) {
        if (item == null)
            return this;
        var copy = new ArrayList<>(items == null ? List.<CourseQuestionItem>of() : items);
        copy.add(item);
        return this.toBuilder().items(List.copyOf(copy)).build();
    }
}
