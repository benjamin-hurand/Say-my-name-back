// src/main/java/com/saymyname/service/course/KnowledgeSelectionService.java
package com.saymyname.service.course;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.saymyname.core.exception.course.NoMoreQuestionsException;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.KnowledgeCandidate;
import com.saymyname.core.model.enums.PoolType;

@Service
public class KnowledgeSelectionService {

    public record SelectionResult(KnowledgeCandidate candidate, PoolType poolType) {
    }

    public record MultiTargetConstraints(
            int maxErrorStreak,
            double maxAvgRtMs,
            double maxHelpRecent,
            double minAttemptsRecent,
            int fetchFactor) {
    }

    private static final MultiTargetConstraints DEFAULT_CONSTRAINTS = new MultiTargetConstraints(
            0,
            9000,
            1.0,
            0.0,
            3);

    private static final int LAZY_SEED_BATCH_SIZE = 5;

    private final KnowledgeService knowledgeService;

    public KnowledgeSelectionService(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    public SelectionResult findNextDueSingleTarget(
            Course course,
            Long lastPersonId,
            boolean allowRepeat,
            Map<PoolType, Double> poolWeights) {

        if (course == null) {
            return null;
        }

        // IMPORTANT: weights must be mutable because we remove pools when empty.
        final Map<PoolType, Double> weights = (poolWeights != null && !poolWeights.isEmpty())
                ? new LinkedHashMap<>(poolWeights)
                : new LinkedHashMap<>(defaultWeights());

        Random rnd = new Random();

        while (!weights.isEmpty()) {
            double sum = weights.values().stream().mapToDouble(Double::doubleValue).sum();
            double r = rnd.nextDouble() * sum;

            PoolType selected = null;
            for (var e : weights.entrySet()) {
                r -= e.getValue();
                if (r <= 0) {
                    selected = e.getKey();
                    break;
                }
            }
            if (selected == null) {
                selected = weights.keySet().iterator().next();
            }

            KnowledgeCandidate c = switch (selected) {
                case ERROR_RECENT -> knowledgeService.findFirstRecentErrorCandidate(course, lastPersonId, allowRepeat);
                case SRS_DUE -> knowledgeService.findFirstSRSCandidate(course, lastPersonId, allowRepeat);
                case DISCOVERED -> knowledgeService.findFirstDiscoveredCandidate(course, lastPersonId, allowRepeat);
                case NEW -> knowledgeService.findFirstNewCandidate(course, lastPersonId, allowRepeat);
                default -> knowledgeService.findRevisionCandidate(course, lastPersonId, allowRepeat);
            };

            if (c != null) {
                return new SelectionResult(c, selected);
            }

            // remove this pool and try again with remaining pools
            weights.remove(selected);
        }

        if (!allowRepeat) {
            int seeded = knowledgeService.ensureSeed(course, LAZY_SEED_BATCH_SIZE);
            if (seeded > 0) {
                return findNextDueSingleTarget(course, lastPersonId, false, poolWeights);
            }
            return findNextDueSingleTarget(course, lastPersonId, true, poolWeights);
        }

        throw new NoMoreQuestionsException(course.getId());
    }

    public List<KnowledgeCandidate> findNextDueMultiTargets(
            Course course,
            KnowledgeCandidate primary,
            int targetCount,
            Long lastPersonId,
            MultiTargetConstraints constraints) {

        if (course == null || targetCount <= 0) {
            return List.of();
        }

        MultiTargetConstraints c = constraints != null ? constraints : DEFAULT_CONSTRAINTS;

        Long primaryPersonId = (primary != null) ? primary.personId() : null;

        List<KnowledgeCandidate> candidates = knowledgeService.findNextDueMultiCandidates(
                course,
                primaryPersonId,
                lastPersonId,
                targetCount,
                c.maxErrorStreak(),
                c.maxAvgRtMs(),
                c.maxHelpRecent(),
                c.minAttemptsRecent(),
                c.fetchFactor());

        LinkedHashMap<Long, KnowledgeCandidate> uniqueByPerson = new LinkedHashMap<>();
        for (KnowledgeCandidate kc : candidates) {
            if (kc == null || kc.personId() == null) {
                continue;
            }
            Long personId = kc.personId();

            if (primaryPersonId != null && primaryPersonId.equals(personId)) {
                continue;
            }
            if (lastPersonId != null && lastPersonId.equals(personId)) {
                continue;
            }

            uniqueByPerson.putIfAbsent(personId, kc);
            if (uniqueByPerson.size() >= targetCount) {
                break;
            }
        }

        return new ArrayList<>(uniqueByPerson.values());
    }

    private Map<PoolType, Double> defaultWeights() {
        // Map.of(...) is immutable, but that's fine as long as we copy it into a
        // mutable map before modifying.
        return Map.of(
                PoolType.ERROR_RECENT, 5.0,
                PoolType.SRS_DUE, 4.0,
                PoolType.NEW, 3.0,
                PoolType.DISCOVERED, 6.0,
                PoolType.REVISION, 0.0);
    }
}