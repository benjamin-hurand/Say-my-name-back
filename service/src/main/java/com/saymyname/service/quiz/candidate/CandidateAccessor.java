// service/quiz/candidate/CandidateAccessor.java
package com.saymyname.service.quiz.candidate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.exception.quiz.QuizUnprocessableException;
import com.saymyname.core.model.quiz.candidate.CandidateQuery;
import com.saymyname.core.model.quiz.candidate.CandidateSample;
import com.saymyname.core.model.quiz.candidate.EligibilityStats;
import com.saymyname.core.model.quiz.candidate.PayloadItem;
import com.saymyname.persistence.dao.quiz.CandidateDao;

/**
 * Business wrapper over CandidateDao.
 * Guarantees excludeSelf is always applied.
 * Provides eligibility counting and candidate sampling with minimal payload.
 */
@Service
public class CandidateAccessor {

    private final CandidateDao candidateDao;

    public CandidateAccessor(CandidateDao candidateDao) {
        this.candidateDao = Objects.requireNonNull(candidateDao, "candidateDao");
    }

    /**
     * Count eligibility stats for format planning.
     * Enforces excludeSelf constraint.
     *
     * @param query the candidate query (excludePersonId should be set to userId)
     * @return eligibility stats with counts
     */
    @Transactional(readOnly = true)
    public EligibilityStats countEligibility(CandidateQuery query) {
        Objects.requireNonNull(query, "query");
        validateExcludeSelf(query);
        return candidateDao.countEligible(query);
    }

    /**
     * Sample candidates with minimal payload for question building.
     * Enforces excludeSelf constraint.
     *
     * @param query       the candidate query
     * @param targetCount how many candidates to sample
     * @return CandidateSample with first item as target
     * @throws QuizUnprocessableException if no candidates available
     */
    @Transactional(readOnly = true)
    public CandidateSample sample(CandidateQuery query, int targetCount) {
        Objects.requireNonNull(query, "query");
        validateExcludeSelf(query);

        // Adjust query limit if needed
        CandidateQuery adjustedQuery = adjustLimit(query, targetCount);

        List<PayloadItem> items = candidateDao.sampleWithPayload(adjustedQuery);

        if (items == null || items.isEmpty()) {
            throw new QuizUnprocessableException(
                    QuizUnprocessableException.ErrorCode.NO_CANDIDATE,
                    "No candidate available for current constraints");
        }

        // First item is the target (random due to shuffle in DAO)
        PayloadItem target = items.get(0);
        return new CandidateSample(items, target.personId(), target.photoStorageKey());
    }

    /**
     * Sample candidates with a specific target person.
     * Useful when target is pre-determined (e.g., Course with Knowledge-based
     * selection).
     *
     * @param query          the candidate query
     * @param targetPersonId the designated target person
     * @param totalCount     total candidates to sample (including target)
     * @return CandidateSample with specified target
     * @throws QuizUnprocessableException if no candidates available
     */
    @Transactional(readOnly = true)
    public CandidateSample sampleWithTarget(CandidateQuery query, Long targetPersonId, int totalCount) {
        Objects.requireNonNull(query, "query");
        Objects.requireNonNull(targetPersonId, "targetPersonId");
        validateExcludeSelf(query);

        if (Objects.equals(targetPersonId, query.getExcludePersonId())) {
            throw new IllegalArgumentException("targetPersonId must differ from excludePersonId");
        }

        if (!candidateDao.isEligiblePerson(query, targetPersonId)) {
            throw new QuizUnprocessableException(
                    QuizUnprocessableException.ErrorCode.NO_CANDIDATE,
                    "Target person " + targetPersonId + " not in eligible candidate pool");
        }

        PayloadItem target = candidateDao.fetchPayloadForPerson(
                targetPersonId,
                query.getGameModeAttributeIds());

        int desired = Math.max(1, totalCount);
        CandidateQuery adjustedQuery = adjustLimit(query, desired + 1);
        List<PayloadItem> items = candidateDao.sampleWithPayload(adjustedQuery);

        List<PayloadItem> distractors = items == null ? List.of()
                : items.stream()
                        .filter(i -> i != null && !targetPersonId.equals(i.personId()))
                        .toList();

        if (distractors.size() > desired - 1) {
            distractors = distractors.subList(0, desired - 1);
        }

        List<PayloadItem> combined = new ArrayList<>(1 + distractors.size());
        combined.add(target);
        combined.addAll(distractors);

        return new CandidateSample(combined, target.personId(), target.photoStorageKey());
    }

    /**
     * Validate that excludeSelf is set (business rule: user cannot be a candidate).
     */
    private void validateExcludeSelf(CandidateQuery query) {
        if (query.getExcludePersonId() == null) {
            throw new IllegalArgumentException(
                    "excludePersonId (userId) must be set to prevent self-questions");
        }
    }

    /**
     * Adjust query limit if targetCount is different from query limit.
     */
    private CandidateQuery adjustLimit(CandidateQuery query, int targetCount) {
        if (query.getLimit() != null && query.getLimit() >= targetCount) {
            return query;
        }
        // Rebuild query with adjusted limit
        return CandidateQuery.builder()
                .userId(query.getUserId())
                .excludePersonId(query.getExcludePersonId())
                .populationScope(query.getPopulationScope())
                .category(query.getCategoryAttributeId(), query.getCategoryValue())
                .requireApprovedPhoto(query.isRequireApprovedPhoto())
                .requireCategoryMatch(query.isRequireCategoryMatch())
                .limit(targetCount)
                .seed(query.getSeed())
                .gameModeId(query.getGameModeId())
                .gameModeAttributeIds(query.getGameModeAttributeIds())
                .attributeOperator(query.getAttributeOperator())
                .countOnly(query.isCountOnly())
                .build();
    }
}
