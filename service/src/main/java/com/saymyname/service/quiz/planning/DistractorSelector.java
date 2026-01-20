package com.saymyname.service.quiz.planning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.quiz.planning.DifficultyProfile;
import com.saymyname.core.model.quiz.planning.QuestionPlan.DistractorStrategy;

/**
 * Selects distractors based on target difficulty using Levenshtein distance.
 *
 * EASY (difficulty < 0.3): Select most distinct names (maximize distance)
 * MEDIUM (difficulty 0.3-0.6): Mixed selection (1 similar + N-1 distinct)
 * HARD (difficulty > 0.6): Select most similar names (minimize distance)
 */
@Service
public class DistractorSelector {

    private static final double EASY_THRESHOLD = 0.3;
    private static final double HARD_THRESHOLD = 0.6;

    /**
     * Result of distractor selection including strategy used and average similarity.
     */
    public record DistractorSelection(
            List<Person> distractors,
            DistractorStrategy strategy,
            double avgSimilarity
    ) {}

    /**
     * Select distractors based on target difficulty.
     *
     * @param primary      The primary candidate (correct answer)
     * @param pool         Pool of potential distractors
     * @param count        Number of distractors needed
     * @param target       Difficulty profile driving selection
     * @return DistractorSelection with selected distractors and metadata
     */
    public DistractorSelection selectDistractors(
            Person primary,
            List<Person> pool,
            int count,
            DifficultyProfile target) {

        if (pool == null || pool.isEmpty() || count <= 0) {
            return new DistractorSelection(List.of(), DistractorStrategy.DISTINCT, 0.0);
        }

        String primaryName = getDisplayName(primary);

        // Score all candidates by similarity
        List<ScoredCandidate> scored = pool.stream()
                .filter(p -> !p.getId().equals(primary.getId()))
                .filter(p -> !isExactMatch(primaryName, getDisplayName(p)))
                .map(p -> new ScoredCandidate(p, computeSimilarity(primaryName, getDisplayName(p))))
                .sorted(Comparator.comparingDouble(ScoredCandidate::similarity))
                .collect(Collectors.toList());

        if (scored.isEmpty()) {
            return new DistractorSelection(List.of(), DistractorStrategy.DISTINCT, 0.0);
        }

        double d = target.targetDifficulty();
        List<Person> selected;
        DistractorStrategy strategy;

        if (d < EASY_THRESHOLD) {
            // EASY: lowest similarity (most distinct)
            selected = scored.stream()
                    .limit(count)
                    .map(ScoredCandidate::person)
                    .collect(Collectors.toList());
            strategy = DistractorStrategy.DISTINCT;
        } else if (d < HARD_THRESHOLD) {
            // MEDIUM: 1 similar + (count-1) distinct
            selected = selectMixed(scored, count);
            strategy = DistractorStrategy.MIXED;
        } else {
            // HARD: highest similarity (most confusing)
            selected = scored.stream()
                    .skip(Math.max(0, scored.size() - count))
                    .map(ScoredCandidate::person)
                    .collect(Collectors.toList());
            strategy = DistractorStrategy.SIMILAR;
        }

        // Compute average similarity of selected distractors
        double avgSimilarity = selected.stream()
                .mapToDouble(p -> computeSimilarity(primaryName, getDisplayName(p)))
                .average()
                .orElse(0.0);

        return new DistractorSelection(selected, strategy, avgSimilarity);
    }

    /**
     * Mixed selection: 1 similar + (count-1) distinct.
     */
    private List<Person> selectMixed(List<ScoredCandidate> scored, int count) {
        List<Person> result = new ArrayList<>();

        // Add one similar (from the end - highest similarity)
        if (!scored.isEmpty()) {
            result.add(scored.get(scored.size() - 1).person());
        }

        // Add distinct ones (from the start - lowest similarity)
        for (int i = 0; i < count - 1 && i < scored.size() - 1; i++) {
            result.add(scored.get(i).person());
        }

        return result;
    }

    /**
     * Get display name for a person (what users see and need to distinguish).
     * Uses primary field attributes concatenated together.
     */
    private String getDisplayName(Person person) {
        if (person == null || person.getAttributes() == null) {
            return "";
        }
        // Concatenate all primary field attribute values
        return person.getAttributes().stream()
                .filter(pa -> pa.getAttribute() != null && pa.getAttribute().isPrimaryField())
                .map(pa -> pa.getValue())
                .filter(v -> v != null && !v.isBlank())
                .collect(Collectors.joining(" "));
    }

    /**
     * Check if two names are exact matches (case-insensitive).
     */
    private boolean isExactMatch(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return a.equalsIgnoreCase(b);
    }

    /**
     * Compute similarity between two names.
     * Returns a value between 0.0 (completely different) and 1.0 (identical).
     */
    private double computeSimilarity(String a, String b) {
        if (a == null || b == null) {
            return 0.0;
        }
        int distance = levenshteinDistance(a.toLowerCase(), b.toLowerCase());
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) {
            return 1.0;
        }
        return 1.0 - ((double) distance / maxLen);
    }

    /**
     * Classic Levenshtein distance implementation.
     * Computes the minimum number of single-character edits (insertions, deletions,
     * or substitutions) required to change one word into the other.
     */
    private int levenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        // Use two rows instead of full matrix for memory efficiency
        int[] prev = new int[len2 + 1];
        int[] curr = new int[len2 + 1];

        // Initialize first row
        for (int j = 0; j <= len2; j++) {
            prev[j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            curr[0] = i;
            for (int j = 1; j <= len2; j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(
                        Math.min(prev[j] + 1, curr[j - 1] + 1),
                        prev[j - 1] + cost
                );
            }
            // Swap rows
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }

        return prev[len2];
    }

    /**
     * Internal record for scoring candidates.
     */
    private record ScoredCandidate(Person person, double similarity) {}
}
