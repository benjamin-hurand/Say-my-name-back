// service/quiz/candidate/CandidateAccessor.java
package com.saymyname.service.quiz.candidate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    @Transactional(readOnly = true)
    public EligibilityStats countEligibility(CandidateQuery query) {
        Objects.requireNonNull(query, "query");
        validateExcludeSelf(query);
        return candidateDao.countEligible(query);
    }

    @Transactional(readOnly = true)
    public CandidateSample sample(CandidateQuery query, int targetCount) {
        Objects.requireNonNull(query, "query");
        validateExcludeSelf(query);

        CandidateQuery adjustedQuery = withMinLimit(query, Math.max(1, targetCount));
        List<PayloadItem> items = candidateDao.sampleWithPayload(adjustedQuery);

        if (items == null || items.isEmpty()) {
            throw new QuizUnprocessableException(
                    QuizUnprocessableException.ErrorCode.NO_CANDIDATE,
                    "No candidate available for current constraints");
        }

        PayloadItem target = items.get(0);
        return new CandidateSample(items, target.personId(), target.photoStorageKey());
    }

    /**
     * Samples a target person's distractor pool, using
     * {@code preferenceAttributeId} as a soft distractor-plausibility signal
     * (e.g. GENDER) instead of a hard filter:
     *
     * <ol>
     * <li>If the target has a value for that attribute, distractors are
     * sampled preferring persons sharing that same value.</li>
     * <li>If that preferred pool doesn't yield enough distractors, it is
     * widened with unrestricted candidates to reach {@code totalCount}.</li>
     * <li>If the target has no value for that attribute (or
     * {@code preferenceAttributeId} is null), or {@code query} already carries
     * its own hard category filter, behaves exactly like an unrestricted
     * sample — no gender-based narrowing is layered on top of an
     * explicit admin-chosen filter.</li>
     * </ol>
     */
    @Transactional(readOnly = true)
    public CandidateSample sampleWithTargetPreferringAttribute(
            CandidateQuery query,
            Long targetPersonId,
            int totalCount,
            Long preferenceAttributeId) {

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

        PayloadItem target = candidateDao.fetchPayloadForPerson(targetPersonId, query.getAttributeId());
        if (target == null) {
            throw new QuizUnprocessableException(
                    QuizUnprocessableException.ErrorCode.NO_CANDIDATE,
                    "Target person " + targetPersonId + " payload not available");
        }

        int desired = Math.max(1, totalCount);
        int distractorsNeeded = desired - 1;

        List<PayloadItem> distractors = new ArrayList<>();
        Set<Long> taken = new HashSet<>();
        taken.add(targetPersonId);

        boolean canPreferAttribute = distractorsNeeded > 0
                && preferenceAttributeId != null
                && query.getCategoryAttributeId() == null;

        if (canPreferAttribute) {
            String preferenceValue = candidateDao.fetchSingleAttributeValue(targetPersonId, preferenceAttributeId);
            if (preferenceValue != null && !preferenceValue.isBlank()) {
                CandidateQuery preferredQuery = withPreferenceCategory(
                        query, preferenceAttributeId, preferenceValue, distractorsNeeded + 1);
                List<PayloadItem> preferred = candidateDao.sampleWithPayload(preferredQuery);
                for (PayloadItem item : preferred) {
                    if (item == null || item.personId() == null || taken.contains(item.personId())) {
                        continue;
                    }
                    distractors.add(item);
                    taken.add(item.personId());
                    if (distractors.size() >= distractorsNeeded) {
                        break;
                    }
                }
            }
        }

        // Widen: preference alone wasn't enough (or didn't apply) — top up from
        // the unrestricted pool so the question can still be built.
        if (distractors.size() < distractorsNeeded) {
            int missing = distractorsNeeded - distractors.size();
            CandidateQuery fallbackQuery = withMinLimit(query, missing + taken.size());
            List<PayloadItem> fallback = candidateDao.sampleWithPayload(fallbackQuery);
            for (PayloadItem item : fallback) {
                if (item == null || item.personId() == null || taken.contains(item.personId())) {
                    continue;
                }
                distractors.add(item);
                taken.add(item.personId());
                if (distractors.size() >= distractorsNeeded) {
                    break;
                }
            }
        }

        List<PayloadItem> combined = new ArrayList<>(1 + distractors.size());
        combined.add(target);
        combined.addAll(distractors);

        return new CandidateSample(combined, target.personId(), target.photoStorageKey());
    }

    private void validateExcludeSelf(CandidateQuery query) {
        if (query.getExcludePersonId() == null) {
            throw new IllegalArgumentException(
                    "excludePersonId (userId) must be set to prevent self-questions");
        }
    }

    /**
     * Returns a query identical to input, but overridden to require a category
     * match on {@code attributeId}/{@code value} (used to bias sampling toward
     * a preference such as matching GENDER), with limit >= minLimit.
     */
    private CandidateQuery withPreferenceCategory(CandidateQuery query, Long attributeId, String value,
            int minLimit) {
        Integer current = query.getLimit();
        int limit = (current != null && current > minLimit) ? current : minLimit;

        return new CandidateQuery.Builder()
                .withUserId(query.getUserId())
                .withExcludePersonId(query.getExcludePersonId())
                .withPopulationScope(query.getPopulationScope())
                .withCategory(attributeId, value)
                .requireApprovedPhoto(query.isRequireApprovedPhoto())
                .requireCategoryMatch(true)
                .withLimit(limit)
                .withSeed(query.getSeed())
                .withAttributeId(query.getAttributeId())
                .countOnly(query.isCountOnly())
                .build();
    }

    /**
     * Returns a query identical to input, but ensures limit >= minLimit.
     */
    private CandidateQuery withMinLimit(CandidateQuery query, int minLimit) {
        Integer current = query.getLimit();
        if (current != null && current >= minLimit) {
            return query;
        }

        return new CandidateQuery.Builder()
                .withUserId(query.getUserId())
                .withExcludePersonId(query.getExcludePersonId())
                .withPopulationScope(query.getPopulationScope())
                .withCategory(query.getCategoryAttributeId(), query.getCategoryValue())
                .requireApprovedPhoto(query.isRequireApprovedPhoto())
                .requireCategoryMatch(query.isRequireCategoryMatch())
                .withLimit(minLimit)
                .withSeed(query.getSeed())
                .withAttributeId(query.getAttributeId()) // ✅ single attributeId
                .countOnly(query.isCountOnly())
                .build();
    }
}