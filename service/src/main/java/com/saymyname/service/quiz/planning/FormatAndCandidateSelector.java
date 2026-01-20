package com.saymyname.service.quiz.planning;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.quiz.planning.DifficultyProfile;
import com.saymyname.core.model.quiz.planning.LearnerState;
import com.saymyname.core.model.quiz.planning.LearningObjective;
import com.saymyname.core.model.quiz.planning.PlanningContext;
import com.saymyname.core.model.quiz.planning.PlanningRequest;
import com.saymyname.core.model.quiz.planning.QuestionPlan.DistractorStrategy;
import com.saymyname.persistence.dao.PersonDao;
import com.saymyname.service.quiz.QuizCandidateProvider;
import com.saymyname.service.quiz.planning.DistractorSelector.DistractorSelection;

/**
 * Selects format and candidates together based on difficulty.
 * This is the core coupled selection that ensures difficulty = f(format, candidates).
 *
 * Format selection rules based on difficulty:
 * - d < 0.25: MCQ (4 choices) - easiest recognition
 * - d 0.25-0.45: CLOZE - partial recall
 * - d 0.45-0.60: HANGMAN - progressive hints
 * - d 0.60-0.75: MCQ with similar distractors - hard recognition
 * - d > 0.75: TEXT_INPUT - full recall
 */
@Service
public class FormatAndCandidateSelector {

    private static final int DEFAULT_POOL_SIZE = 30;
    private static final int DEFAULT_DISTRACTOR_COUNT = 3;

    // Format difficulty bands
    private static final List<FormatDifficultyBand> FORMAT_BANDS = List.of(
            new FormatDifficultyBand(0.00, 0.25, QuizFormat.MCQ),
            new FormatDifficultyBand(0.25, 0.45, QuizFormat.CLOZE),
            new FormatDifficultyBand(0.45, 0.60, QuizFormat.HANGMAN),
            new FormatDifficultyBand(0.60, 0.75, QuizFormat.MCQ),
            new FormatDifficultyBand(0.75, 1.00, QuizFormat.TEXT_INPUT)
    );

    // Typical difficulty per format (for finding closest allowed format)
    private static final Map<QuizFormat, Double> FORMAT_DIFFICULTIES = Map.of(
            QuizFormat.MCQ, 0.25,
            QuizFormat.CLOZE, 0.35,
            QuizFormat.HANGMAN, 0.50,
            QuizFormat.WORD_PUZZLE, 0.55,
            QuizFormat.TEXT_INPUT, 0.80,
            QuizFormat.ORDERING, 0.60,
            QuizFormat.ASSOCIATION, 0.65,
            QuizFormat.BINARY_SWIPE, 0.30
    );

    private final PersonDao personDao;
    private final QuizCandidateProvider candidateProvider;
    private final DistractorSelector distractorSelector;

    public FormatAndCandidateSelector(
            PersonDao personDao,
            QuizCandidateProvider candidateProvider,
            DistractorSelector distractorSelector) {
        this.personDao = personDao;
        this.candidateProvider = candidateProvider;
        this.distractorSelector = distractorSelector;
    }

    /**
     * Result of coupled format + candidate selection.
     */
    public record FormatAndCandidates(
            QuizFormat format,
            Person primaryCandidate,
            List<Person> distractors,
            List<Long> candidatePoolIds,
            DistractorStrategy selectionStrategy,
            double avgDistractorSimilarity
    ) {}

    /**
     * Select format and candidates together based on difficulty profile.
     */
    public FormatAndCandidates select(
            DifficultyProfile target,
            LearnerState state,
            PlanningRequest request,
            LearningObjective objective) {
        return selectInternal(target, state, request, objective, null);
    }

    /**
     * Select candidates for an explicit format choice.
     */
    public FormatAndCandidates selectWithFormat(
            QuizFormat format,
            DifficultyProfile target,
            LearnerState state,
            PlanningRequest request,
            LearningObjective objective) {
        return selectInternal(target, state, request, objective, format);
    }

    private FormatAndCandidates selectInternal(
            DifficultyProfile target,
            LearnerState state,
            PlanningRequest request,
            LearningObjective objective,
            QuizFormat forcedFormat) {

        PlanningContext context = request.context();
        LearnerState effectiveState = state != null ? state : LearnerState.withDefaults();

        // STEP A: SELECT FORMAT BASED ON DIFFICULTY + CONTEXT CONSTRAINTS
        QuizFormat format = forcedFormat != null
                ? forcedFormat
                : selectFormat(target.targetDifficulty(), context, effectiveState, objective);

        // STEP B: SELECT PRIMARY CANDIDATE
        Person primary = selectPrimaryCandidate(request);

        // STEP C: SELECT CANDIDATE POOL AND DISTRACTORS (difficulty-aware)
        List<Person> pool = candidateProvider.candidates(
                request.gameOptions(), request.userId(), DEFAULT_POOL_SIZE);

        List<Long> poolIds = pool.stream()
                .map(Person::getId)
                .collect(Collectors.toList());

        // For MCQ, select distractors based on difficulty
        List<Person> distractors = List.of();
        DistractorStrategy strategy = DistractorStrategy.DISTINCT;
        double avgSimilarity = 0.0;

        if (format == QuizFormat.MCQ) {
            DistractorSelection selection = distractorSelector.selectDistractors(
                    primary, pool, DEFAULT_DISTRACTOR_COUNT, target);
            distractors = selection.distractors();
            strategy = selection.strategy();
            avgSimilarity = selection.avgSimilarity();
        }

        return new FormatAndCandidates(
                format, primary, distractors, poolIds, strategy, avgSimilarity
        );
    }

    /**
     * Select format based on difficulty, respecting context.allowedFormats().
     */
    private QuizFormat selectFormat(
            double difficulty,
            PlanningContext context,
            LearnerState state,
            LearningObjective objective) {
        // Multi-target objectives should force ORDERING/ASSOCIATION when allowed.
        if (objective != null && objective.requiresMultiTarget() && context.multiTargetAllowed()) {
            QuizFormat multiTarget = selectMultiTargetFormat(state, context);
            if (multiTarget != null) {
                return multiTarget;
            }
        }

        // Handle format variation if same format streak is high
        if (context.sessionTracking()
                && state.formatStreak() >= 3
                && state.lastFormat() != null
                && !state.showsFatigueSigns()) {
            QuizFormat varied = nextFormatForVariation(state.lastFormat(), context);
            if (varied != null && context.isFormatAllowed(varied)) {
                return varied;
            }
        }

        // Find ideal format for this difficulty
        QuizFormat idealFormat = FORMAT_BANDS.stream()
                .filter(band -> band.contains(difficulty))
                .map(FormatDifficultyBand::format)
                .findFirst()
                .orElse(QuizFormat.MCQ);

        // If context has no format constraints, use ideal
        Set<QuizFormat> allowed = context.allowedFormats();
        if (allowed == null || allowed.isEmpty()) {
            return idealFormat;
        }

        // If ideal is allowed, use it
        if (allowed.contains(idealFormat)) {
            return idealFormat;
        }

        // Find closest allowed format by difficulty
        return findClosestAllowedFormat(difficulty, allowed);
    }

    private QuizFormat selectMultiTargetFormat(LearnerState state, PlanningContext context) {
        QuizFormat last = state.lastFormat();
        if (last == QuizFormat.ORDERING && context.isFormatAllowed(QuizFormat.ASSOCIATION)) {
            return QuizFormat.ASSOCIATION;
        }
        if (last == QuizFormat.ASSOCIATION && context.isFormatAllowed(QuizFormat.ORDERING)) {
            return QuizFormat.ORDERING;
        }
        if (context.isFormatAllowed(QuizFormat.ORDERING)) {
            return QuizFormat.ORDERING;
        }
        if (context.isFormatAllowed(QuizFormat.ASSOCIATION)) {
            return QuizFormat.ASSOCIATION;
        }
        return null;
    }

    /**
     * Find the allowed format closest to the target difficulty.
     */
    private QuizFormat findClosestAllowedFormat(double targetDifficulty, Set<QuizFormat> allowed) {
        return allowed.stream()
                .min(Comparator.comparingDouble(f ->
                        Math.abs(FORMAT_DIFFICULTIES.getOrDefault(f, 0.5) - targetDifficulty)))
                .orElse(QuizFormat.MCQ);
    }

    /**
     * Get next format for variety (anti-monotony).
     * MCQ -> CLOZE -> TEXT_INPUT -> MCQ
     */
    private QuizFormat nextFormatForVariation(QuizFormat current, PlanningContext context) {
        QuizFormat next = switch (current) {
            case MCQ -> QuizFormat.CLOZE;
            case CLOZE -> QuizFormat.TEXT_INPUT;
            case TEXT_INPUT -> QuizFormat.MCQ;
            case HANGMAN -> QuizFormat.CLOZE;
            case WORD_PUZZLE -> QuizFormat.HANGMAN;
            case BINARY_SWIPE -> QuizFormat.MCQ;
            case ORDERING -> QuizFormat.ASSOCIATION;
            case ASSOCIATION -> QuizFormat.ORDERING;
        };

        if (context.isFormatAllowed(next)) {
            return next;
        }
        return null;
    }

    /**
     * Select primary candidate (the correct answer person).
     */
    private Person selectPrimaryCandidate(PlanningRequest request) {
        Knowledge knowledge = request.primaryKnowledge();
        if (knowledge != null && knowledge.getPerson() != null) {
            // COURSE mode: use pre-selected Knowledge's person
            return knowledge.getPerson();
        }

        // TRAINING mode: get first from options-based query
        if (request.gameOptions() != null) {
            List<Person> persons = personDao.findByOptions(request.gameOptions(), request.userId());
            if (!persons.isEmpty()) {
                // Skip last person if we are avoiding repeat
                if (request.lastPersonId() != null) {
                    return persons.stream()
                            .filter(p -> !p.getId().equals(request.lastPersonId()))
                            .findFirst()
                            .orElse(persons.get(0));
                }
                return persons.get(0);
            }
        }

        throw new IllegalStateException("No candidate available for question planning");
    }

    /**
     * Internal record for format-difficulty band mapping.
     */
    private record FormatDifficultyBand(double min, double max, QuizFormat format) {
        boolean contains(double d) {
            return d >= min && d < max;
        }
    }
}
