package com.saymyname.service.quiz.candidate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.saymyname.core.exception.quiz.QuizUnprocessableException;
import com.saymyname.core.model.quiz.candidate.CandidateQuery;
import com.saymyname.core.model.quiz.candidate.CandidateSample;
import com.saymyname.core.model.quiz.candidate.PayloadItem;
import com.saymyname.persistence.dao.quiz.CandidateDao;

/**
 * Covers the GENDER-as-distractor-preference behavior of
 * {@link CandidateAccessor#sampleWithTargetPreferringAttribute}: prefer a
 * same-value pool, widen to the unrestricted pool when it's too small, and
 * never filter when the signal is absent or an explicit category filter is
 * already active.
 */
@ExtendWith(MockitoExtension.class)
class CandidateAccessorTest {

    @Mock
    private CandidateDao candidateDao;

    private static final Long EXCLUDE_ID = 1L;
    private static final Long TARGET_ID = 2L;
    private static final Long ATTR_ID = 10L;
    private static final Long GENDER_ATTR_ID = 20L;

    @Test
    void prefersSameGenderDistractorsWhenPoolIsLargeEnough() {
        CandidateAccessor accessor = new CandidateAccessor(candidateDao);
        CandidateQuery query = baseQuery();

        when(candidateDao.isEligiblePerson(query, TARGET_ID)).thenReturn(true);
        when(candidateDao.fetchPayloadForPerson(TARGET_ID, ATTR_ID)).thenReturn(payload(TARGET_ID));
        when(candidateDao.fetchSingleAttributeValue(TARGET_ID, GENDER_ATTR_ID)).thenReturn("Homme");

        ArgumentCaptor<CandidateQuery> queries = ArgumentCaptor.forClass(CandidateQuery.class);
        when(candidateDao.sampleWithPayload(queries.capture()))
                .thenReturn(List.of(payload(3L), payload(4L), payload(5L)));

        CandidateSample sample = accessor.sampleWithTargetPreferringAttribute(query, TARGET_ID, 3, GENDER_ATTR_ID);

        assertThat(sample.targetPersonId()).isEqualTo(TARGET_ID);
        assertThat(sample.distractors()).extracting(PayloadItem::personId).containsExactly(3L, 4L);

        // Only the preferred (gender-matched) query should have been issued —
        // the pool was large enough, so no unrestricted top-up was needed.
        verify(candidateDao, times(1)).sampleWithPayload(any());
        CandidateQuery preferredQuery = queries.getValue();
        assertThat(preferredQuery.isRequireCategoryMatch()).isTrue();
        assertThat(preferredQuery.getCategoryAttributeId()).isEqualTo(GENDER_ATTR_ID);
        assertThat(preferredQuery.getCategoryValue()).isEqualTo("Homme");
    }

    @Test
    void widensToUnrestrictedPoolWhenPreferredPoolIsTooSmall() {
        CandidateAccessor accessor = new CandidateAccessor(candidateDao);
        CandidateQuery query = baseQuery();

        when(candidateDao.isEligiblePerson(query, TARGET_ID)).thenReturn(true);
        when(candidateDao.fetchPayloadForPerson(TARGET_ID, ATTR_ID)).thenReturn(payload(TARGET_ID));
        when(candidateDao.fetchSingleAttributeValue(TARGET_ID, GENDER_ATTR_ID)).thenReturn("Femme");

        // Preferred (gender-matched) pool only has one other person.
        when(candidateDao.sampleWithPayload(argThatRequiresCategory()))
                .thenReturn(List.of(payload(3L)));
        // Unrestricted widen pool has plenty, including an overlap with the
        // already-picked preferred distractor (must not be duplicated).
        when(candidateDao.sampleWithPayload(argThatHasNoCategory()))
                .thenReturn(List.of(payload(3L), payload(4L), payload(5L)));

        CandidateSample sample = accessor.sampleWithTargetPreferringAttribute(query, TARGET_ID, 3, GENDER_ATTR_ID);

        assertThat(sample.distractors()).extracting(PayloadItem::personId).containsExactly(3L, 4L);
    }

    @Test
    void doesNotFilterWhenTargetHasNoGenderValue() {
        CandidateAccessor accessor = new CandidateAccessor(candidateDao);
        CandidateQuery query = baseQuery();

        when(candidateDao.isEligiblePerson(query, TARGET_ID)).thenReturn(true);
        when(candidateDao.fetchPayloadForPerson(TARGET_ID, ATTR_ID)).thenReturn(payload(TARGET_ID));
        when(candidateDao.fetchSingleAttributeValue(TARGET_ID, GENDER_ATTR_ID)).thenReturn(null);

        when(candidateDao.sampleWithPayload(any())).thenReturn(List.of(payload(3L), payload(4L)));

        CandidateSample sample = accessor.sampleWithTargetPreferringAttribute(query, TARGET_ID, 3, GENDER_ATTR_ID);

        assertThat(sample.distractors()).extracting(PayloadItem::personId).containsExactly(3L, 4L);
        verify(candidateDao).sampleWithPayload(argThatHasNoCategory());
        verify(candidateDao, never()).sampleWithPayload(argThatRequiresCategory());
    }

    @Test
    void doesNotFilterWhenTenantHasNoGenderAttribute() {
        CandidateAccessor accessor = new CandidateAccessor(candidateDao);
        CandidateQuery query = baseQuery();

        when(candidateDao.isEligiblePerson(query, TARGET_ID)).thenReturn(true);
        when(candidateDao.fetchPayloadForPerson(TARGET_ID, ATTR_ID)).thenReturn(payload(TARGET_ID));
        when(candidateDao.sampleWithPayload(any())).thenReturn(List.of(payload(3L), payload(4L)));

        CandidateSample sample = accessor.sampleWithTargetPreferringAttribute(query, TARGET_ID, 3, null);

        assertThat(sample.distractors()).extracting(PayloadItem::personId).containsExactly(3L, 4L);
        verify(candidateDao, never()).fetchSingleAttributeValue(anyLong(), anyLong());
    }

    @Test
    void doesNotLayerGenderPreferenceOverAnExplicitCategoryFilter() {
        CandidateAccessor accessor = new CandidateAccessor(candidateDao);
        CandidateQuery query = new CandidateQuery.Builder()
                .withExcludePersonId(EXCLUDE_ID)
                .withAttributeId(ATTR_ID)
                .withCategory(99L, "TeamA")
                .requireCategoryMatch(true)
                .build();

        when(candidateDao.isEligiblePerson(query, TARGET_ID)).thenReturn(true);
        when(candidateDao.fetchPayloadForPerson(TARGET_ID, ATTR_ID)).thenReturn(payload(TARGET_ID));
        when(candidateDao.sampleWithPayload(any())).thenReturn(List.of(payload(3L), payload(4L)));

        CandidateSample sample = accessor.sampleWithTargetPreferringAttribute(query, TARGET_ID, 3, GENDER_ATTR_ID);

        assertThat(sample.distractors()).extracting(PayloadItem::personId).containsExactly(3L, 4L);
        verify(candidateDao, never()).fetchSingleAttributeValue(anyLong(), anyLong());

        ArgumentCaptor<CandidateQuery> queries = ArgumentCaptor.forClass(CandidateQuery.class);
        verify(candidateDao).sampleWithPayload(queries.capture());
        assertThat(queries.getValue().getCategoryAttributeId()).isEqualTo(99L);
        assertThat(queries.getValue().getCategoryValue()).isEqualTo("TeamA");
    }

    @Test
    void throwsWhenTargetIsNotEligible() {
        CandidateAccessor accessor = new CandidateAccessor(candidateDao);
        CandidateQuery query = baseQuery();

        when(candidateDao.isEligiblePerson(query, TARGET_ID)).thenReturn(false);

        assertThatThrownBy(() -> accessor.sampleWithTargetPreferringAttribute(query, TARGET_ID, 3, GENDER_ATTR_ID))
                .isInstanceOf(QuizUnprocessableException.class);
    }

    private static CandidateQuery baseQuery() {
        return new CandidateQuery.Builder()
                .withExcludePersonId(EXCLUDE_ID)
                .withAttributeId(ATTR_ID)
                .build();
    }

    private static PayloadItem payload(Long personId) {
        return new PayloadItem(personId, Map.of());
    }

    private static CandidateQuery argThatRequiresCategory() {
        return org.mockito.ArgumentMatchers.argThat(q -> q != null && q.isRequireCategoryMatch());
    }

    private static CandidateQuery argThatHasNoCategory() {
        return org.mockito.ArgumentMatchers.argThat(q -> q != null && q.getCategoryAttributeId() == null);
    }
}
