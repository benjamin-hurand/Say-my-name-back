package com.saymyname.service.course;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.saymyname.core.exception.course.NoMoreQuestionsException;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.PoolType;

@Service
public class KnowledgeSelectionService {

    public record SelectionResult(Knowledge knowledge, PoolType poolType) {
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

        Map<PoolType, Double> weights = (poolWeights != null && !poolWeights.isEmpty())
                ? new LinkedHashMap<>(poolWeights)
                : defaultWeights();

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

            Knowledge k = switch (selected) {
                case ERROR_RECENT -> knowledgeService.findFirstRecentError(course, lastPersonId, allowRepeat);
                case SRS_DUE -> knowledgeService.findFirstSRS(course, lastPersonId, allowRepeat);
                case DISCOVERED -> knowledgeService.findFirstDiscovered(course, lastPersonId, allowRepeat);
                case NEW -> knowledgeService.findFirstNew(course, lastPersonId, allowRepeat);
                default -> knowledgeService.findRevision(course, lastPersonId, allowRepeat);
            };

            if (k != null) {
                return new SelectionResult(k, selected);
            }

            weights.remove(selected);
        }

        if (allowRepeat) {
            throw new NoMoreQuestionsException(course.getId());
        }

        return findNextDueSingleTarget(course, lastPersonId, true, poolWeights);
    }

    public List<Knowledge> findNextDueMultiTargets(
            Course course,
            Knowledge primary,
            int targetCount,
            Long lastPersonId,
            MultiTargetConstraints constraints) {
        if (course == null || targetCount <= 0) {
            return List.of();
        }

        MultiTargetConstraints c = constraints != null ? constraints : DEFAULT_CONSTRAINTS;

        Long primaryPersonId = primary != null && primary.getPerson() != null ? primary.getPerson().getId() : null;

        List<Knowledge> candidates = knowledgeService.findNextDueMulti(
                course,
                primaryPersonId,
                lastPersonId,
                targetCount,
                c.maxErrorStreak(),
                c.maxAvgRtMs(),
                c.maxHelpRecent(),
                c.minAttemptsRecent(),
                c.fetchFactor());

        LinkedHashMap<Long, Knowledge> uniqueByPerson = new LinkedHashMap<>();
        for (Knowledge k : candidates) {
            if (k == null || k.getPerson() == null || k.getPerson().getId() == null) {
                continue;
            }
            Long personId = k.getPerson().getId();
            if (primaryPersonId != null && primaryPersonId.equals(personId)) {
                continue;
            }
            if (lastPersonId != null && lastPersonId.equals(personId)) {
                continue;
            }
            uniqueByPerson.putIfAbsent(personId, k);
            if (uniqueByPerson.size() >= targetCount) {
                break;
            }
        }

        return new ArrayList<>(uniqueByPerson.values());
    }

    private Map<PoolType, Double> defaultWeights() {
        return Map.of(
                PoolType.ERROR_RECENT, 5.0,
                PoolType.SRS_DUE, 4.0,
                PoolType.NEW, 3.0,
                PoolType.DISCOVERED, 6.0,
                PoolType.REVISION, 0.0);
    }
}
