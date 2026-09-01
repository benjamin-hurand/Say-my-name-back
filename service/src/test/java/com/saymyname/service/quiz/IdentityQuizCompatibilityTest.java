package com.saymyname.service.quiz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.course.KnowledgeResultEvent;
import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.Fact;
import com.saymyname.core.model.quiz.candidate.CandidateQuery;
import com.saymyname.core.model.quiz.candidate.PayloadItem;
import com.saymyname.core.util.AnswerValidator;
import com.saymyname.persistence.dao.FactDao;
import com.saymyname.persistence.dao.course.KnowledgeDao;
import com.saymyname.persistence.dao.quiz.CandidateDao;
import com.saymyname.service.course.KnowledgeService;
import com.saymyname.service.course.scheduler.SchedulerStrategy;
import com.saymyname.service.leaderboard.LeaderboardService;
import com.saymyname.service.quiz.candidate.CandidateAccessor;

@ExtendWith(MockitoExtension.class)
class IdentityQuizCompatibilityTest {

    @Mock
    private CandidateDao candidateDao;
    @Mock
    private FactDao factDao;
    @Mock
    private KnowledgeDao knowledgeDao;
    @Mock
    private SchedulerStrategy scheduler;
    @Mock
    private LeaderboardService leaderboardService;

    @Test
    void identityFactFlowsThroughCandidateAnswerAndKnowledgeComponents() {
        Long identityAttributeId = 13L;
        Long identityFactId = 30L;
        Long personId = 7L;

        CandidateQuery query = new CandidateQuery.Builder()
                .withExcludePersonId(99L)
                .withAttributeId(identityAttributeId)
                .withLimit(1)
                .build();
        PayloadItem payload = new PayloadItem(personId, Map.of(identityAttributeId, "Jean DUPONT"));
        when(candidateDao.sampleWithPayload(any(CandidateQuery.class))).thenReturn(List.of(payload));

        var sample = new CandidateAccessor(candidateDao).sample(query, 1);
        assertThat(sample.targetPersonId()).isEqualTo(personId);
        assertThat(sample.items().getFirst().attributeValue(identityAttributeId)).isEqualTo("Jean DUPONT");

        Attribute identityAttribute = new Attribute.Builder().withId(identityAttributeId).build();
        Fact identityFact = new Fact.Builder()
                .withId(identityFactId)
                .withPersonId(personId)
                .withAttribute(identityAttribute)
                .withValue("Jean DUPONT")
                .build();
        when(factDao.findAttributesByPersonId(personId)).thenReturn(List.of(identityFact));
        String answerKey = new DefaultAnswerKeyService(factDao).compute(personId, identityAttributeId);
        assertThat(AnswerValidator.match("Jean DUPONT", List.of(answerKey), "AND", false)).isTrue();

        User user = new User.Builder().withId(5L).withSrsAlgorithm(SrsAlgorithm.SM2).build();
        when(knowledgeDao.findByUserAndFact(user.getId(), identityFactId)).thenReturn(Optional.empty());
        when(factDao.findById(identityFactId)).thenReturn(Optional.of(identityFact));
        when(scheduler.mapGrade(true)).thenReturn(5);
        when(leaderboardService.computeXpForKnowledgeResult(true, false)).thenReturn(0);
        KnowledgeService knowledgeService = new KnowledgeService(
                knowledgeDao,
                Map.of(SrsAlgorithm.SM2, scheduler),
                SrsAlgorithm.SM2,
                factDao,
                leaderboardService);

        knowledgeService.recordBatchResults(user, List.of(new KnowledgeResultEvent.Builder()
                .withFactId(identityFactId)
                .withCorrect(true)
                .build()));

        ArgumentCaptor<Knowledge> knowledge = ArgumentCaptor.forClass(Knowledge.class);
        verify(knowledgeDao).upsertKnowledge(knowledge.capture());
        assertThat(knowledge.getValue().getFactId()).isEqualTo(identityFactId);
    }
}
