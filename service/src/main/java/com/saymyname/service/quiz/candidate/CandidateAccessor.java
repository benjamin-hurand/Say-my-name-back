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

        // ✅ single attributeId now
        PayloadItem target = candidateDao.fetchPayloadForPerson(targetPersonId, query.getAttributeId());
        if (target == null) {
            throw new QuizUnprocessableException(
                    QuizUnprocessableException.ErrorCode.NO_CANDIDATE,
                    "Target person " + targetPersonId + " payload not available");
        }

        int desired = Math.max(1, totalCount);

        // +1 pour augmenter les chances de récupérer assez de distractors après
        // filtrage
        CandidateQuery adjustedQuery = withMinLimit(query, desired + 1);
        List<PayloadItem> items = candidateDao.sampleWithPayload(adjustedQuery);

        List<PayloadItem> distractors = (items == null) ? List.of()
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

    private void validateExcludeSelf(CandidateQuery query) {
        if (query.getExcludePersonId() == null) {
            throw new IllegalArgumentException(
                    "excludePersonId (userId) must be set to prevent self-questions");
        }
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